Clínica Oasis es una clínica médica de tamaño mediano ubicada en la ciudad de León, Guanajuato, México. 
En el marco de un proyecto de vinculación con empresa real, el equipo propuso y desarrolló una aplicación móvil 
nativa para Android que digitaliza la gestión de servicios de salud entre la clínica y sus pacientes. 
La propuesta fue aceptada por la dirección de la institución, que facilitó acceso a la información operativa necesaria 
para modelar el sistema.

El proyecto implementó satisfactoriamente todos los requisitos técnicos establecidos en la rúbrica de evaluación: 
arquitectura MVVM con Clean Architecture, sincronización bidireccional entre Firebase Realtime Database (API REST) y 
Room Database (caché offline), inyección de dependencias con Hilt, manejo de estados reactivos con StateFlow, y 
seguridad de sesión con DataStore.

La aplicación ofrece tres niveles de acceso (paciente, médico y administrador), reflejando la estructura operativa 
real de Clínica Oasis, donde trabajan 6 médicos distribuidos en 3 consultorios con horarios definidos. 
El sistema permite agendar citas, consultar historial médico, gestionar datos clínicos y revisar comunicados institucionales, 
garantizando disponibilidad offline y experiencia de usuario fluida sin crashes.
