-- ─────────────────────────────────────────────────────────────────────────────
-- Script de actualización para el nuevo flujo de citas
-- Ejecutar en Workbench sobre la BD "empresa"
-- ─────────────────────────────────────────────────────────────────────────────

USE empresa;

-- 1. Actualizar el ENUM de estado en la tabla cita
ALTER TABLE cita
  MODIFY COLUMN estado ENUM('en_espera','pre_aprobada','confirmada','rechazada')
  NOT NULL DEFAULT 'en_espera';

-- 2. Crear tabla horario_disponible
CREATE TABLE IF NOT EXISTS horario_disponible (
  id_horario  INT      NOT NULL AUTO_INCREMENT,
  fecha       DATE     NOT NULL,
  hora        TIME     NOT NULL,
  ocupado     BOOLEAN  NOT NULL DEFAULT FALSE,
  id_cita     INT      NULL,
  CONSTRAINT pk_horario PRIMARY KEY (id_horario),
  CONSTRAINT fk_horario_cita FOREIGN KEY (id_cita)
    REFERENCES cita(id_cita) ON DELETE SET NULL
);

-- 3. Agregar columna id_horario a la tabla cita
ALTER TABLE cita
  ADD COLUMN IF NOT EXISTS id_horario INT NULL,
  ADD CONSTRAINT fk_cita_horario FOREIGN KEY (id_horario)
    REFERENCES horario_disponible(id_horario) ON DELETE SET NULL;

-- Verificar
SELECT 'OK - Script ejecutado correctamente' AS resultado;
