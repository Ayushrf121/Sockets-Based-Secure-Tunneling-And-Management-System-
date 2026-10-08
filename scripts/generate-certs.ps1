# Generates the server keystore and the client truststore for Secure Proxy.
# Usage (from the project root):
#   $env:PROXY_TLS_PASS = "choose-a-password"
#   powershell -ExecutionPolicy Bypass -File scripts/generate-certs.ps1

if (-not $env:PROXY_TLS_PASS -or $env:PROXY_TLS_PASS.Length -lt 6) {
    Write-Error 'Set $env:PROXY_TLS_PASS first (at least 6 characters).'
    exit 1
}

if (Test-Path "certs/server.p12") {
    Write-Error "certs/server.p12 already exists. Regenerating would invalidate every client truststore. Delete the certs folder yourself if you really want new certificates."
    exit 1
}

$pass = $env:PROXY_TLS_PASS
New-Item -ItemType Directory -Force -Path certs | Out-Null

Write-Host "1/3 Creating server keypair..."
keytool -genkeypair -alias secureproxy -keyalg RSA -keysize 2048 -validity 365 `
    -storetype PKCS12 -keystore certs/server.p12 -storepass $pass -keypass $pass `
    -dname "CN=SecureProxy" -ext "san=dns:localhost,ip:127.0.0.1"

Write-Host "2/3 Exporting public certificate..."
keytool -exportcert -alias secureproxy -keystore certs/server.p12 -storepass $pass -file certs/server.cer

Write-Host "3/3 Creating client truststore (pinned to this certificate)..."
keytool -importcert -alias secureproxy -file certs/server.cer `
    -keystore certs/client-truststore.p12 -storetype PKCS12 -storepass $pass -noprompt

Write-Host ""
Write-Host "Done. Files in certs/ :"
Get-ChildItem certs | Select-Object Name, Length
Write-Host "Copy ONLY server.cer to other computers (never server.p12)."