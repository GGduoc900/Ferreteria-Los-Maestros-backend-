package cl.ferreteria.inventario.service;

import cl.ferreteria.inventario.dto.ItemStockRequest;
import cl.ferreteria.inventario.dto.LineaStock;
import cl.ferreteria.inventario.dto.ResumenInventario;
import cl.ferreteria.inventario.dto.StockProducto;
import cl.ferreteria.inventario.model.Producto;
import cl.ferreteria.inventario.repository.ProductoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Lógica de negocio del inventario. Es la fuente de verdad de productos, precios y stock.
 *
 * <p>Reglas principales:
 *
 * <ul>
 *   <li>El código es único, se escribe en mayúsculas y tiene el formato de 2 letras + 3 dígitos
 *       (ej. MC001).
 *   <li>Precios y stock no pueden ser negativos y el precio de venta no puede ser menor al de compra.
 *   <li>Un producto tiene "stock bajo" cuando stock ≤ stock mínimo.
 *   <li>Descontar stock es atómico: si un solo producto no alcanza, no se descuenta nada.
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    private static final Pattern FORMATO_CODIGO = Pattern.compile("^[A-Z]{2}\\d{3}$");

    private final ProductoRepository repo;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    // ------------------------------------------------------------------ lectura

    public List<Producto> listar(String categoria, String texto, boolean soloStockBajo) {
        String q = normalizar(texto);
        return repo.findAll(Sort.by("codigo")).stream()
                .filter(p -> categoria == null || categoria.isBlank() || categoria.equalsIgnoreCase(p.getCategoria()))
                .filter(p -> q.isEmpty() || coincide(p, q))
                .filter(p -> !soloStockBajo || tieneStockBajo(p))
                .toList();
    }

    public Producto obtener(String codigo) {
        return repo.findById(codigo.toUpperCase())
                .orElseThrow(() -> new NoSuchElementException("Producto " + codigo + " no encontrado"));
    }

    public StockProducto consultarStock(String codigo) {
        return aStock(obtener(codigo));
    }

    public ResumenInventario resumen() {
        List<Producto> todos = repo.findAll();
        long bajo = todos.stream().filter(this::tieneStockBajo).count();
        long agotados = todos.stream().filter(p -> p.getStock() == 0).count();
        long valor = todos.stream().mapToLong(p -> (long) p.getStock() * p.getPrecioCompra()).sum();
        return new ResumenInventario(todos.size(), bajo, agotados, valor);
    }

    // ------------------------------------------------------------------ CRUD

    @Transactional
    public Producto crear(Producto producto) {
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        producto.setCodigo(producto.getCodigo().trim().toUpperCase());
        validar(producto);
        if (repo.existsById(producto.getCodigo())) {
            throw new IllegalStateException("Ya existe un producto con el código " + producto.getCodigo());
        }
        return repo.save(producto);
    }

    @Transactional
    public Producto actualizar(String codigo, Producto datos) {
        Producto actual = obtener(codigo);
        datos.setCodigo(actual.getCodigo()); // el código no se puede cambiar
        validar(datos);
        return repo.save(datos);
    }

    @Transactional
    public void eliminar(String codigo) {
        repo.delete(obtener(codigo));
    }

    // ------------------------------------------------------------------ stock

    @Transactional
    public StockProducto ajustarStock(String codigo, Integer stock) {
        if (stock == null || stock < 0) throw new IllegalArgumentException("El stock debe ser un número ≥ 0");
        Producto p = obtener(codigo);
        p.setStock(stock);
        return aStock(repo.save(p));
    }

    @Transactional
    public StockProducto ajustarStockMinimo(String codigo, Integer stockMinimo) {
        if (stockMinimo == null || stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo debe ser un número ≥ 0");
        }
        Producto p = obtener(codigo);
        p.setStockMinimo(stockMinimo);
        return aStock(repo.save(p));
    }

    /**
     * Descuenta el stock de varios productos en una sola transacción (lo usa el microservicio de
     * ventas). Primero valida todo y recién después modifica: si algún producto no existe o no
     * alcanza, lanza error y no se descuenta nada. Devuelve el precio de venta vigente de cada uno
     * (el cliente nunca envía precios).
     */
    @Transactional
    public List<LineaStock> descontar(List<ItemStockRequest> items) {
        Map<String, Integer> pedido = agrupar(items);
        List<Producto> productos = new ArrayList<>();
        for (var e : pedido.entrySet()) {
            Producto p =
                    repo.bloquearPorCodigo(e.getKey())
                            .orElseThrow(() -> new NoSuchElementException("Producto " + e.getKey() + " no encontrado"));
            if (p.getStock() < e.getValue()) {
                throw new IllegalStateException(
                        "Stock insuficiente para " + p.getNombre() + " (disponible: " + p.getStock() + ")");
            }
            productos.add(p);
        }
        List<LineaStock> lineas = new ArrayList<>();
        for (Producto p : productos) {
            int cantidad = pedido.get(p.getCodigo());
            p.setStock(p.getStock() - cantidad);
            repo.save(p);
            lineas.add(new LineaStock(p.getCodigo(), p.getNombre(), cantidad, p.getPrecioVenta(), p.getStock()));
        }
        return lineas;
    }

    /** Devuelve stock (anulación de una venta o recepción de mercadería). */
    @Transactional
    public List<LineaStock> reponer(List<ItemStockRequest> items) {
        List<LineaStock> lineas = new ArrayList<>();
        for (var e : agrupar(items).entrySet()) {
            Producto p =
                    repo.bloquearPorCodigo(e.getKey())
                            .orElseThrow(() -> new NoSuchElementException("Producto " + e.getKey() + " no encontrado"));
            p.setStock(p.getStock() + e.getValue());
            repo.save(p);
            lineas.add(new LineaStock(p.getCodigo(), p.getNombre(), e.getValue(), p.getPrecioVenta(), p.getStock()));
        }
        return lineas;
    }

    // ------------------------------------------------------------------ apoyo

    private void validar(Producto p) {
        if (!FORMATO_CODIGO.matcher(p.getCodigo()).matches()) {
            throw new IllegalArgumentException("El código debe tener 2 letras y 3 números (ej. MC001)");
        }
        requerido(p.getCategoria(), "categoría");
        requerido(p.getSubcategoria(), "subcategoría");
        requerido(p.getNombre(), "nombre");
        requerido(p.getMarca(), "marca");
        requerido(p.getUnidad(), "unidad");
        noNegativo(p.getPrecioCompra(), "precio de compra");
        noNegativo(p.getPrecioVenta(), "precio de venta");
        noNegativo(p.getStock(), "stock");
        noNegativo(p.getStockMinimo(), "stock mínimo");
        if (p.getPrecioVenta() == 0) throw new IllegalArgumentException("El precio de venta debe ser mayor a 0");
        if (p.getPrecioVenta() < p.getPrecioCompra()) {
            throw new IllegalArgumentException("El precio de venta no puede ser menor al precio de compra");
        }
        if (p.getNombre().length() > 120) throw new IllegalArgumentException("El nombre admite hasta 120 caracteres");
    }

    private static void requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
    }

    private static void noNegativo(Integer valor, String campo) {
        if (valor == null || valor < 0) throw new IllegalArgumentException("El " + campo + " debe ser un número ≥ 0");
    }

    private static Map<String, Integer> agrupar(List<ItemStockRequest> items) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Debes indicar al menos un producto");
        Map<String, Integer> pedido = new LinkedHashMap<>();
        for (ItemStockRequest i : items) {
            if (i.codigo() == null || i.codigo().isBlank()) throw new IllegalArgumentException("Falta el código de un producto");
            if (i.cantidad() == null || i.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de " + i.codigo() + " debe ser mayor a 0");
            }
            pedido.merge(i.codigo().trim().toUpperCase(), i.cantidad(), Integer::sum);
        }
        return pedido;
    }

    private boolean tieneStockBajo(Producto p) {
        return p.getStock() <= p.getStockMinimo();
    }

    private static boolean coincide(Producto p, String q) {
        return normalizar(p.getNombre() + " " + p.getMarca() + " " + p.getCodigo() + " " + p.getSubcategoria())
                .contains(q);
    }

    /** Minúsculas y sin tildes: "caneria" encuentra "Cañería". */
    static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    private StockProducto aStock(Producto p) {
        return new StockProducto(
                p.getCodigo(), p.getNombre(), p.getStock(), p.getStockMinimo(), tieneStockBajo(p), p.getStock() == 0);
    }
}
