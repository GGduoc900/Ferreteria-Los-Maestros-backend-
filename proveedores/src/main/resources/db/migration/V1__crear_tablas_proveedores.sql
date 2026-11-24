-- =====================================================================
-- Proveedores · V1 · Esquema inicial
-- proveedor 1 ── N orden_compra 1 ── N orden_compra_item
-- =====================================================================
CREATE TABLE proveedor (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    rut       VARCHAR(12)  NOT NULL,
    nombre    VARCHAR(120) NOT NULL,
    correo    VARCHAR(120),
    telefono  VARCHAR(20),
    estado    VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_proveedor PRIMARY KEY (id),
    CONSTRAINT uq_proveedor_rut UNIQUE (rut)
);

CREATE TABLE orden_compra (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    id_proveedor  BIGINT       NOT NULL,
    estado        VARCHAR(20)  NOT NULL,
    fecha         TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_orden_compra PRIMARY KEY (id),
    CONSTRAINT fk_orden_compra_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id)
);

CREATE TABLE orden_compra_item (
    orden_compra_id  BIGINT      NOT NULL,
    codigo           VARCHAR(10) NOT NULL,
    cantidad         INT         NOT NULL,
    costo_unitario   INT         NOT NULL,
    CONSTRAINT fk_oc_item_orden FOREIGN KEY (orden_compra_id) REFERENCES orden_compra (id) ON DELETE CASCADE
);
