-- =====================================================================
-- Inventario · V1 · Esquema inicial
-- Compatible con MySQL 8 y H2 (modo MySQL).
-- =====================================================================
CREATE TABLE producto (
    codigo        VARCHAR(10)  NOT NULL,
    imagen        VARCHAR(255),
    categoria     VARCHAR(60)  NOT NULL,
    subcategoria  VARCHAR(60)  NOT NULL,
    nombre        VARCHAR(120) NOT NULL,
    marca         VARCHAR(60)  NOT NULL,
    unidad        VARCHAR(30)  NOT NULL,
    precio_compra INT          NOT NULL,
    precio_venta  INT          NOT NULL,
    stock         INT          NOT NULL DEFAULT 0,
    stock_minimo  INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_producto PRIMARY KEY (codigo),
    CONSTRAINT ck_producto_precios CHECK (precio_compra >= 0 AND precio_venta >= precio_compra),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0 AND stock_minimo >= 0)
);

CREATE INDEX ix_producto_categoria ON producto (categoria);
