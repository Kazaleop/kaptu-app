$base = "https://backend-production-23c83fa9.up.railway.app"

$checks = @(
  "/health",
  "/api/v1/imoveis",
  "/api/v1/locatarios",
  "/api/v1/contratos",
  "/api/v1/documentos",
  "/api/v1/financeiro/transacoes",
  "/api/v1/dashboard/resumo",
  "/api/v1/alertas"
)

foreach ($path in $checks) {
  try {
    $r = Invoke-WebRequest -Uri ($base + $path) -UseBasicParsing -TimeoutSec 60
    Write-Output ("OK   " + $path + " -> " + $r.StatusCode)
  } catch {
    Write-Output ("FAIL " + $path + " -> " + $_.Exception.Message)
  }
}
