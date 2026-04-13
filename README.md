# Cyber-Defense-Duel
Protyecto #1 Algoritmos y Estructuras de datos i

## Descripción
Cyber Defense Duel es un proyecto que utiliza una arquitectura cliente-servidor para permitir la conexión de múltiples jugadores en red local. Este documento describe los pasos necesarios para ejecutar correctamente la aplicación.

---

## Requisitos

Antes de ejecutar el proyecto, asegúrese de contar con:

- Java Development Kit (JDK 21)
- Apache Maven
- Visual Studio Code (o cualquier IDE compatible con Java)

Además, verifique que Java y Maven estén correctamente configurados en las variables de entorno del sistema.

---

## Ejecución del Servidor

1. Abrir el proyecto en el entorno de desarrollo.
2. Navegar hasta el archivo:

   ```
   GameScreen/src/main/java/server/GameServer
   ```

3. Localizar el método `main`.
4. Ejecutar la aplicación seleccionando la opción **Run main**.

### Resultado esperado

En la consola se debe mostrar el siguiente mensaje:

```
Cyber Defense Duel - Servidor iniciado en puerto 5000
```

---

## Conexión de los Clientes

### 1. Conexión a la red

Asegurarse de que todas las máquinas (servidor y clientes) estén conectadas a la misma red local.

---

### 2. Configuración de la IP

#### En el servidor:
1. Presionar `Windows + R`.
2. Escribir `cmd` y presionar Enter.
3. Ejecutar el comando:

   ```
   ipconfig
   ```

4. Localizar la **Dirección IPv4**.

#### En el cliente:
1. Abrir el archivo:

   ```
   GameScreen/src/main/java/ui/LoginScreen
   ```

2. Buscar la línea donde aparece `"localhost"`.
3. Reemplazar `"localhost"` por la dirección IPv4 obtenida, por ejemplo:

   ```java
   "192.168.1.10"
   ```

---

### 3. Ejecución del Cliente

1. Abrir una terminal en la carpeta `GameScreen`.
2. Ejecutar el siguiente comando:

   ```
   mvn javafx:run
   ```

3. Esperar a que la aplicación se inicie.

### Resultado esperado

Se abrirá la interfaz gráfica del juego mostrando que el cliente se ha conectado correctamente al servidor.

---

## Consideraciones

- El proceso de configuración del cliente debe realizarse en cada máquina que participará como jugador.
- El servidor debe iniciarse antes que los clientes.
- Verificar que el puerto 5000 no esté bloqueado por el firewall.

---

## Solución de Problemas

Si ocurre algún error de conexión:

- Verifique que la dirección IP sea correcta.
- Asegúrese de que todos los dispositivos estén en la misma red.
- Confirme que el servidor esté en ejecución.
- Revise la configuración del firewall.

---

## Notas

Se recomienda realizar pruebas iniciales en una red local para asegurar el correcto funcionamiento del sistema antes de su uso en entornos más complejos.
