-- =====================================================================
-- Catálogo · V1 · Descripciones comerciales de los productos
-- Los datos del producto (precio, stock) viven en inventario; aquí solo
-- el texto de vitrina, referenciado por el código del producto.
-- =====================================================================
CREATE TABLE descripcion_producto (
    codigo      VARCHAR(10)   NOT NULL,
    descripcion VARCHAR(2000) NOT NULL,
    CONSTRAINT pk_descripcion_producto PRIMARY KEY (codigo)
);
