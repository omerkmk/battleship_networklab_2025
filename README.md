# Battleship Network Lab 2025

**Öğrenciler:** Ömer Kumek
**Ders:** Network Lab  
**Üniversite:** Fatih Sultan Mehmet Vakıf University  

## Proje Yapısı

- `model/` – Oyun mantığı (Position, Ship, Board, GameState)  
- `utils/` – Mesaj serileştirme/parçalama (Message.java)  
- `server/` – Konsol sunucu (GameServer.java)  
- `client/` – GUI & client logic (GameClient.java, GameUI.java)  
- `report/` – Proje raporu dosyaları  

## Derleme & Çalıştırma

```bash
# Derleme
javac -d bin $(find src -name "*.java")

# Sunucuyu başlat
java -cp bin server.GameServer

# İstemciyi başlat
java -cp bin client.GameClient

