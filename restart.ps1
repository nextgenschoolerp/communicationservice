$port = 8090
Write-Host "Stopping any process on port $port..."
$conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if ($conn) {
    $pids = $conn.OwningProcess | Select-Object -Unique
    foreach ($p in $pids) {
        Write-Host "Killing process $p..."
        Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
    }
}
Write-Host "Starting communicationservice on port $port..."
.\gradlew.bat bootRun
