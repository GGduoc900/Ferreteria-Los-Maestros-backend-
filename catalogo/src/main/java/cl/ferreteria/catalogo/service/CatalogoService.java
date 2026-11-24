package cl.ferreteria.catalogo.service;

import cl.ferreteria.catalogo.dto.DetalleProducto;
import cl.ferreteria.catalogo.dto.ListadoCatalogo;
import cl.ferreteria.catalogo.dto.ListadoInventario;
import cl.ferreteria.catalogo.dto.ProductoInventario;
import cl.ferreteria.catalogo.model.DescripcionProducto;
import cl.ferreteria.catalogo.repository.DescripcionProductoRepository;
import cl.ferreteria.shared.web.ClienteInterno;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vitrina pública. Consulta los productos al microservicio de inventario (fuente de verdad de
 * precios y stock) y les agrega la descripción comercial guardada en su propia base de datos.
 * Así el catálogo siempre muestra lo mismo que se vende, sin duplicar datos.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoService {
    private final DescripcionProductoRepository descripciones;
    private final ClienteInterno cliente;
    private final RestClient inventario;

    public CatalogoService(
            DescripcionProductoRepository descripciones,
            ClienteInterno cliente,
            @Value("${servicios.inventario.url}") String urlInventario) {
        this.descripciones = descripciones;
        this.cliente = cliente;
        this.inventario = cliente.para(urlInventario);
    }

    public ListadoCatalogo listar(String texto, String categoria, Boolean disponible) {
        String q = normalizar(texto);
        Map<String, String> textos =
                descripciones.findAll().stream()
                        .collect(Collectors.toMap(DescripcionProducto::getCodigo, DescripcionProducto::getDescripcion));
        List<DetalleProducto> productos =
                productosInventario().stream()
                        .map(p -> DetalleProducto.de(p, textos.get(p.codigo())))
                        .filter(p -> categoria == null || categoria.isBlank() || categoria.equalsIgnoreCase(p.categoria()))
                        .filter(p -> q.isEmpty() || normalizar(p.codigo() + " " + p.nombre() + " " + p.marca() + " " + p.subcategoria()).contains(q))
                        .filter(p -> disponible == null || p.disponible() == disponible)
                        .sorted(Comparator.comparing(DetalleProducto::codigo))
                        .toList();
        return new ListadoCatalogo(productos.size(), productos);
    }

    public DetalleProducto detalle(String codigo) {
        ProductoInventario p =
                cliente.llamar(
                        "inventario",
                        () -> inventario.get().uri("/productos/{codigo}", codigo).retrieve().body(ProductoInventario.class));
        String texto = descripciones.findById(p.codigo()).map(DescripcionProducto::getDescripcion).orElse(null);
        return DetalleProducto.de(p, texto);
    }

    public List<String> categorias() {
        return productosInventario().stream().map(ProductoInventario::categoria).distinct().sorted().toList();
    }

    @Transactional
    public DescripcionProducto editarDescripcion(String codigo, String descripcion) {
        if (descripcion == null || descripcion.isBlank()) throw new IllegalArgumentException("Falta el campo descripcion");
        if (descripcion.length() > 2000) throw new IllegalArgumentException("La descripción admite hasta 2000 caracteres");
        detalle(codigo); // 404 si el producto no existe en inventario
        return descripciones.save(new DescripcionProducto(codigo.toUpperCase(), descripcion.trim()));
    }

    private List<ProductoInventario> productosInventario() {
        ListadoInventario listado =
                cliente.llamar("inventario", () -> inventario.get().uri("/productos").retrieve().body(ListadoInventario.class));
        return listado == null || listado.productos() == null ? List.of() : listado.productos();
    }

    private static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }

}
