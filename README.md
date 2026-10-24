# POS · Librería Bazar Rosita

Sistema de punto de venta para la Librería Bazar Rosita (curso **SOLWEB S10**).
Construido con la arquitectura del proyecto S9 y el diseño del prototipo de Figma,
respaldado por la base de datos `DB_ROSITA`.

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Frontend | Angular 21 · Bootstrap 5.3.8 · `@ng-bootstrap/ng-bootstrap` · bootstrap-icons · jQuery |
| Backend | Spring Boot 4.1.1 · Java 21 · Spring Data JPA |
| Base de datos | MySQL 8.0 (`DB_ROSITA`) |
| Puertos | Frontend `4200` · Backend `8083` |

---

## Estructura

Patrón **MVC + capas**: la **V** (Vista) vive en Angular, el **C** (Controlador) en
`controller/` y el **M** (Modelo) se compone de `model/` + `service/` + `repository/`.

```
S10/
├── db_rosita_v4.sql                  # Esquema completo (26 tablas + 2 vistas)
├── db_rosita_datos_prueba_v1.sql     # Productos, stock y clientes de prueba
├── Historias_Usuario_POS_Libreria_Rosita_v1.3.xlsx
└── pos-rosita/
    ├── backend/                      # Spring Boot (puerto 8083)
    │   └── src/main/java/pe/edu/upn/sowad/posrosita/
    │       ├── controller/           # C · Auth, Producto, SesionCaja, Venta, Cliente
    │       ├── service/              # M · lógica de negocio (transacciones, RN)
    │       ├── repository/           # M · interfaces JPA (acceso a datos)
    │       ├── model/                # M · entidades (@Entity)
    │       ├── dto/                  # M · objetos de intercambio de la venta
    │       └── PosRositaApplication.java
    └── frontend/                     # V · Angular 21 (puerto 4200)
        └── src/app/
            ├── pages/                # V · vistas: Login, Principal (layout), NuevaVenta
            ├── components/modals/    # V · vistas modales: AperturaCaja, Cobro, Boleta...
            ├── services/             # llamadas HTTP al controlador + sesión global
            ├── models/               # interfaces TypeScript (contratos del M)
            └── configuracion/        # URL del API
```

| Capa MVC | Dónde está |
|----------|-----------|
| **Vista** | `frontend/src/app/pages` y `frontend/src/app/components` |
| **Controlador** | `backend/.../controller` (recibe la petición y llama al servicio) |
| **Modelo** | `backend/.../model` + `service` + `repository` |

---

## Cómo ejecutar

### 0. Requisitos
- MySQL 8.0 corriendo con la base `DB_ROSITA` ya creada.
- Java 21 (`JAVA_HOME` debe apuntar a un JDK 21).
- Node.js 24+ con npm.

### 1. Base de datos (solo la primera vez)
```bash
mysql -uroot -p < db_rosita_v4.sql
mysql -uroot -p < db_rosita_datos_prueba_v1.sql
```

### 2. Backend (puerto 8083)
```powershell
cd pos-rosita\backend
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot'
.\mvnw.cmd spring-boot:run
```

### 3. Frontend (puerto 4200)
```powershell
cd pos-rosita\frontend
npm install      # solo la primera vez
npm start
```

### 4. Entrar al sistema
Abra **http://localhost:4200**

| Rol | ID | Clave |
|-----|-----|-------|
| Vendedor (Carlos López) | `123456` | `123456` |
| Dueño (Rosita Ramírez) | `654321` | `654321` |

---

## Flujo de venta (HU-01 a HU-08)

1. **Login** por ID numérico (HU-01).
2. **Apertura de caja**: elegir caja → confirmar → fondo inicial (HU-02).
   No se puede vender con la caja cerrada.
3. **Nueva venta**: buscar productos por nombre o SKU, filtrar por categoría
   y agregarlos al carrito (HU-03). Se valida el stock (RN-001) y no se permite
   un carrito vacío (RN-012).
4. **Cliente opcional** por DNI / CE / RUC (HU-05).
5. **Cobro** (HU-06/07): solo **Efectivo** (con vuelto) o **Transferencia**
   Yape / Plin / Bancaria con número de operación único por canal (RN-023).
   *No existe el método Tarjeta/POS* (decisión del Excel v1.3).
6. **Boleta** imprimible (HU-08). Las ventas menores a S/ 5.00 quedan
   **"Sin comprobante"** (RN-004).
7. El **stock se descuenta** en la misma transacción que la venta (RN-005);
   si algo falla, se revierte todo.

---

## Reglas de negocio implementadas

- **RN-001** Stock insuficiente → se bloquea la venta.
- **RN-004** Venta < S/ 5.00 → estado "Sin comprobante".
- **RN-005** Venta y descuento de stock son una sola transacción.
- **RN-012** Carrito vacío → no se puede cobrar.
- **RN-015** Una caja no se apertura dos veces.
- **RN-023** Número de operación único dentro del mismo canal.

---

## Notas

- `PAGO.canal_unico` es una columna **generada** (`STORED`) en MySQL; el backend
  no la mapea en JPA, MySQL la calcula solo.
- Las contraseñas se guardan como `SHA2(CONCAT('salt_rosita_', clave), 256)`.
- El backend usa `ddl-auto=validate`: el esquema lo define `db_rosita_v4.sql`,
  Hibernate solo lo comprueba.
