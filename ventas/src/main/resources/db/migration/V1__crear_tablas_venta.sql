-- =====================================================================
-- Ventas · V1 · Esquema inicial
-- venta (cabecera) 1 ── N venta_item (detalle con el precio del momento)
-- =====================================================================
CREATE TABLE venta (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    folio           VARCHAR(20),
    correo_cliente  VARCHAR(120) NOT NULL,
    estado          VARCHAR(30)  NOT NULL,
    fecha           TIMESTAMP(6) NOT NULL,
    neto            INT          NOT NULL DEFAULT 0,
    iva             INT          NOT NULL DEFAULT 0,
    total           INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_venta PRIMARY KEY (id),
    CONSTRAINT uq_venta_folio UNIQUE (folio)
);

CREATE INDEX ix_venta_cliente ON venta (correo_cliente);

CREATE TABLE venta_item (
    venta_id        BIGINT       NOT NULL,
    codigo          VARCHAR(10)  NOT NULL,
    nombre          VARCHAR(120),
    cantidad        INT          NOT NULL,
    precio_unitario INT          NOT NULL,
    subtotal        INT          NOT NULL,
    CONSTRAINT fk_venta_item_venta FOREIGN KEY (venta_id) REFERENCES venta (id) ON DELETE CASCADE
);
