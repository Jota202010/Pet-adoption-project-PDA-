CREATE DATABASE IF NOT EXISTS empresa
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
USE empresa;

--Usuario: root
--Port: 3306
--Password:8080
 

CREATE TABLE usuario (
  id_usuario      INT             NOT NULL AUTO_INCREMENT,
  usuario         VARCHAR(50)     NOT NULL,
  nombre          VARCHAR(100)    NOT NULL,
  email           VARCHAR(150)    NOT NULL,
  telefono        VARCHAR(20)     NULL,
  direccion       VARCHAR(255)    NULL,
  contrasena      VARCHAR(255)    NOT NULL,
  rol             ENUM('usuario','administrador')
                  NOT NULL DEFAULT 'usuario',
  fecha_registro  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_usuario   PRIMARY KEY (id_usuario),
  CONSTRAINT uq_usuario   UNIQUE      (usuario),
  CONSTRAINT uq_email     UNIQUE      (email)
);
 

CREATE TABLE perro (
  id_perro          INT          NOT NULL AUTO_INCREMENT,
  nombre            VARCHAR(80)  NOT NULL,
  edad              VARCHAR(30)  NULL,
  sexo              ENUM('Macho','Hembra') NOT NULL,
  estado            ENUM('RESCATADO','ABANDONADO','ACOGIDO')
                    NOT NULL,
  esterilizado      BOOLEAN      NOT NULL DEFAULT FALSE,
  vacunado          BOOLEAN      NOT NULL DEFAULT FALSE,
  descripcion       TEXT         NULL,
  nivel_salud       ENUM('SANO','ENFERMO','CRITICO') NOT NULL,
  sociabilidad      ENUM('ALTA','MEDIA','BAJA')      NOT NULL,
  adoptado          BOOLEAN      NOT NULL DEFAULT FALSE,
  lista_para_adoptar BOOLEAN     NOT NULL DEFAULT FALSE,
  registro_medico   TEXT         NULL,
  fecha_ingreso     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_perro PRIMARY KEY (id_perro)
);
 

CREATE TABLE foto_perro (
  id_foto    INT          NOT NULL AUTO_INCREMENT,
  id_perro   INT          NOT NULL,
  url_foto   VARCHAR(500) NOT NULL,
  es_perfil  BOOLEAN      NOT NULL DEFAULT FALSE,
  orden      INT          NOT NULL DEFAULT 0,
  CONSTRAINT pk_foto    PRIMARY KEY (id_foto),
  CONSTRAINT fk_foto_perro FOREIGN KEY (id_perro)
    REFERENCES perro(id_perro) ON DELETE CASCADE
);
 

CREATE TABLE cita (
  id_cita            INT      NOT NULL AUTO_INCREMENT,
  id_usuario         INT      NOT NULL,
  id_perro           INT      NOT NULL,
  id_admin           INT      NULL,
  estado             ENUM('en_espera','aprobada','rechazada')
                     NOT NULL DEFAULT 'en_espera',
  fecha_cita         DATE     NOT NULL,
  hora_cita          TIME     NOT NULL,
  sede               VARCHAR(150) NULL,
  fecha_solicitud    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_decision     DATETIME NULL,
  -- Informacion del hogar
  tipo_vivienda      ENUM('casa','apartamento') NULL,
  propiedad          ENUM('propia','alquilada') NULL,
  permiten_mascotas  ENUM('si','no','no_aplica') NULL,
  num_personas       INT      NULL,
  todos_acuerdo      BOOLEAN  NULL,
  -- Experiencia con mascotas
  perros_antes       BOOLEAN  NULL,
  mascotas_actual    BOOLEAN  NULL,
  mascotas_anteriores TEXT    NULL,
  -- Tiempo y compromiso
  horas_solo         VARCHAR(30) NULL,
  puede_pasear       BOOLEAN  NULL,
  responsable        VARCHAR(100) NULL,
  -- Capacidad economica
  cubre_vet          BOOLEAN  NULL,
  cubre_emergencias  BOOLEAN  NULL,
  -- Motivacion
  motivacion         TEXT     NULL,
  tipo_perro_buscado TEXT     NULL,
  -- Condiciones aceptadas
  cond_no_abandono   BOOLEAN  NOT NULL DEFAULT FALSE,
  cond_seguimiento   BOOLEAN  NOT NULL DEFAULT FALSE,
  cond_evaluacion    BOOLEAN  NOT NULL DEFAULT FALSE,
  CONSTRAINT pk_cita       PRIMARY KEY (id_cita),
  CONSTRAINT fk_cita_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario(id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_cita_perro FOREIGN KEY (id_perro)
    REFERENCES perro(id_perro) ON DELETE CASCADE,
  CONSTRAINT fk_cita_admin FOREIGN KEY (id_admin)
    REFERENCES usuario(id_usuario) ON DELETE SET NULL
);
 
