-- =====================================================================
-- Pagos · V1 · Esquema inicial
-- id_venta referencia a una venta del microservicio de ventas (otra base
-- de datos), por eso no hay FOREIGN KEY: la integridad se valida por API.
-- =====================================================================
CREATE TABLE pago (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    id_venta        BIGINT       NOT NULL,
    correo_cliente  VARCHAR(120) NOT NULL,
    metodo          VARCHAR(30)  NOT NULL,
    monto           INT          NOT NULL,
    referencia      VARCHAR(60),
    estado          VARCHAR(20)  NOT NULL,
    fecha           TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_pago PRIMARY KEY (id),
    CONSTRAINT ck_pago_monto CHECK (monto > 0)
);

CREATE INDEX ix_pago_venta ON pago (id_venta);
