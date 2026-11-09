# UPI Payment Simulator (Java Swing)

Educational mini-project for OOP in Java. It uses virtual balances only and does not connect to banks, NPCI, UPI rails or payment gateways.

## Features
- Register unique UPI IDs and login with a 4–6 digit demo PIN
- Three incorrect PIN attempts lock the account
- Check balance, send virtual money, create pending money requests
- Rs. 10,000 daily outgoing transfer limit
- Transaction history and five-record mini statement
- Synchronized payment processing and rollback/reversal if receiver credit fails
- HashMap, ArrayList, Iterator, custom exceptions, SwingWorker and a background autosave thread
- Local Java serialization in `data/upi-data.ser`

## Requirements
JDK 17 or later.

## Run (Windows)
Open a terminal in this folder:
```bat
javac -d out src\com\upisimulator\Main.java src\com\upisimulator\model\*.java src\com\upisimulator\service\*.java src\com\upisimulator\exception\*.java src\com\upisimulator\ui\*.java src\com\upisimulator\util\*.java
java -cp out com.upisimulator.Main
```
On macOS/Linux replace `\` with `/` in the compile command.

## Demo accounts
Create accounts from the login window. Example:
- ganesh@demo / PIN 1234 / opening balance Rs. 5000
- nandini@demo / PIN 1234 / opening balance Rs. 5000

## Packages
- `model`: users, accounts, payment records, requests and enums
- `service`: payment rules and synchronized operations
- `exception`: user-defined exceptions
- `ui`: Swing login and dashboard
- `util`: file persistence and autosave thread

## Important limitations
PINs are stored as plain text in this academic demonstration. Do not use real PINs or real money. Production systems require secure credential hashing, bank integrations, strong authorization, audit controls and regulated payment infrastructure.
