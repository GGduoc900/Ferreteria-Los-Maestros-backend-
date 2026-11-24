-- =====================================================================
-- Notificaciones · V1 · Esquema inicial
-- Guarda las consultas del formulario de contacto (tipo CONTACTO) y el
-- historial de correos enviados/simulados (tipo CORREO).
-- =====================================================================
CREATE TABLE notificacion (
    id        BIGINT        NOT NULL AUTO_INCREMENT,
    tipo      VARCHAR(20)   NOT NULL,
    nombre    VARCHAR(100),
    correo    VARCHAR(120),
    telefono  VARCHAR(20),
    para      VARCHAR(120),
    asunto    VARCHAR(150),
    mensaje   VARCHAR(2000) NOT NULL,
    estado    VARCHAR(20)   NOT NULL,
    fecha     TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_notificacion PRIMARY KEY (id)
);

CREATE INDEX ix_notificacion_tipo ON notificacion (tipo);
