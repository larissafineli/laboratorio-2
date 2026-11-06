# Sobe o back-end conectado ao Supabase. Uso:  .\rodar-supabase.ps1
# Voce cola a "connection string" do Supabase (Session pooler) e digita a senha do banco.

Write-Host ""
Write-Host "No Supabase: botao Connect (topo) > aba 'Session pooler' > copie a linha que comeca com postgresql://" -ForegroundColor Cyan
$uri = Read-Host "Cole a connection string aqui"
$senhaSegura = Read-Host "Digite a SENHA do banco (a que voce criou no projeto)" -AsSecureString
$senha = [System.Net.NetworkCredential]::new("", $senhaSegura).Password

# Formato esperado: postgresql://USUARIO:[SENHA]@HOST:PORTA/BANCO
if ($uri -notmatch '^postgres(?:ql)?://([^:@]+)(?::[^@]*)?@([^:/]+):(\d+)/(\w+)') {
    Write-Host "Nao entendi essa connection string. Ela deve parecer com:" -ForegroundColor Red
    Write-Host "postgresql://postgres.abcdef:[YOUR-PASSWORD]@aws-0-sa-east-1.pooler.supabase.com:5432/postgres"
    exit 1
}
$usuario = $Matches[1]; $host_ = $Matches[2]; $porta = $Matches[3]; $banco = $Matches[4]

$env:SUPABASE_DB_URL = "jdbc:postgresql://${host_}:${porta}/${banco}?sslmode=require"
$env:SUPABASE_DB_USER = $usuario
$env:SUPABASE_DB_PASSWORD = $senha
$env:APP_SEED = "true"   # cria usuarios de teste so se o banco estiver vazio

Write-Host "Conectando em $host_ como $usuario ..." -ForegroundColor Green

Set-Location (Join-Path $PSScriptRoot "backend")
# Usa o Maven Wrapper do projeto (nao precisa ter Maven instalado).
# Retire o -Djava.version=21 se voce instalar o JDK 25.
& .\mvnw.cmd "-Djava.version=21" clean spring-boot:run
