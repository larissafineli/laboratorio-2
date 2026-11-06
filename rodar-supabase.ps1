# Sobe o back-end conectado ao Supabase. Uso:  .\rodar-supabase.ps1
# Voce cola a "connection string" do Supabase (Session pooler) e digita a senha do banco.

Write-Host ""
# Se existir supabase.local.txt (fora do git), le a connection string de la.
$arquivo = Join-Path $PSScriptRoot "supabase.local.txt"
if (Test-Path $arquivo) {
    $uri = (Get-Content $arquivo -Raw).Trim()
    Write-Host "Usando a connection string de supabase.local.txt" -ForegroundColor Cyan
} else {
    Write-Host "No Supabase: botao Connect (topo) > aba 'Session pooler' > copie a linha que comeca com postgresql://" -ForegroundColor Cyan
    Write-Host "Para colar no terminal use o botao direito do mouse (Ctrl+V pode nao funcionar)." -ForegroundColor Yellow
    $uri = Read-Host "Cole a connection string aqui"
}
Write-Host "A senha vai aparecer na tela para voce conferir o que digitou. Use botao direito para colar." -ForegroundColor Yellow
$senha = (Read-Host "Digite a SENHA do banco (a que voce criou no projeto)").Trim()
if ($senha.Length -lt 6 -or $senha -match '\^V') {
    Write-Host "Senha vazia ou invalida (digitou ^V?). Digite a senha manualmente ou use o botao direito para colar." -ForegroundColor Red
    exit 1
}
Write-Host ("Senha recebida com " + $senha.Length + " caracteres.") -ForegroundColor Cyan

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

# Usa o JDK 25 se estiver instalado (terminais abertos antes da instalacao ainda enxergam o Java antigo).
$jdk25 = Get-ChildItem "C:\Program Files\Microsoft", "C:\Program Files\Java", "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-25*" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jdk25) {
    $env:JAVA_HOME = $jdk25.FullName
    $env:Path = "$($jdk25.FullName)\bin;$env:Path"
}
Write-Host ("Java em uso: " + ((& java -version 2>&1) | Select-Object -First 1)) -ForegroundColor Cyan

Set-Location (Join-Path $PSScriptRoot "backend")
# Usa o Maven Wrapper do projeto (nao precisa ter Maven instalado).
& .\mvnw.cmd clean spring-boot:run
