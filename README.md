# GOOD-FOOD-TODAY

Proyecto lúdico-educativo durante el bench de GFT

## Revisar calidad

Desde la raiz del repositorio:

```powershell
.\revisar.cmd
```

El comando ejecuta los tests del backend con Maven, construye el frontend y ejecuta los tests de Angular en modo CI. Si no existe `frontend/node_modules`, instala dependencias con `npm ci` antes de revisar.
