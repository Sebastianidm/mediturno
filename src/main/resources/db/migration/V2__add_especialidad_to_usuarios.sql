-- Agregar columna especialidad a la tabla de usuarios para soporte de médicos
ALTER TABLE usuarios ADD COLUMN especialidad VARCHAR(100);
