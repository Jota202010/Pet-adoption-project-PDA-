-- =====================================================================
-- init.sql  ·  Inicialización de la BD de tienda-service (tienda_refugio)
--
-- MySQL lo ejecuta automáticamente SOLO la primera vez que se crea el
-- volumen de datos (carpeta /docker-entrypoint-initdb.d/).
--
-- Es IDEMPOTENTE: se puede ejecutar N veces sin duplicar cuentas,
-- sin tocar saldos existentes y sin borrar nada.
--
-- Nota: en el primer arranque la tabla `cuenta` todavía no existe
-- (la crea Hibernate con ddl-auto=update cuando arranca tienda-service),
-- por eso este script la crea con CREATE TABLE IF NOT EXISTS usando la
-- MISMA estructura que la entidad Cuenta.java. Cuando Hibernate arranca,
-- detecta que ya existe y solo completa lo que falte (p. ej. el UNIQUE
-- de `tipo`).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS tienda_refugio
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tienda_refugio;

-- Estructura equivalente a la entidad Cuenta.java
CREATE TABLE IF NOT EXISTS cuenta (
    id_cuenta      INT            NOT NULL AUTO_INCREMENT,
    titular        VARCHAR(100)   NOT NULL,
    saldo          DECIMAL(15,2)  NOT NULL DEFAULT 0.00,
    tipo           ENUM('REFUGIO','TIENDA') NOT NULL,
    fecha_creacion DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id_cuenta)
) ENGINE=InnoDB;

-- Si la tabla ya existía (creada por Hibernate sin DEFAULT), se le da el
-- DEFAULT para que el INSERT de abajo no falle. Repetirlo es inofensivo.
ALTER TABLE cuenta
    MODIFY COLUMN fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

-- Cuenta del REFUGIO: 500.000 COP (solo si no existe)
INSERT INTO cuenta (titular, saldo, tipo)
SELECT 'Refugio Nueva Vida', 500000.00, 'REFUGIO' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cuenta WHERE tipo = 'REFUGIO');

-- Cuenta de la TIENDA: 0 COP (solo si no existe)
INSERT INTO cuenta (titular, saldo, tipo)
SELECT 'Tienda del Refugio', 0.00, 'TIENDA' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cuenta WHERE tipo = 'TIENDA');
