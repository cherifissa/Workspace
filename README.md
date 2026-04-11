# Projet Interoperabilite 2025-2026

Ce workspace contient deux projets:

- `java-project/`: implementation Java (H2 + pont Java/Python + fonctions appelees depuis C++)
- `cpp-project/`: executable C++ (JNI) + bibliotheque native (`bridge`) + protobuf C++ genere

Le projet suit les consignes du sujet:

- H2 avec tables virtuelles `CUSTOMER`, `ORDER`, `ORDERDETAILS`
- `MOYENNE()` reimplementee en Java (agregat SQL custom)
- `ECARTTYPE()` via Java -> Python/Numpy (`numpy.std`)
- Reimplementation cote C++ avec JVM creee depuis C++, fonctions C++ appelees depuis SQL Java
- Filtrage question 5 avec un unique parametre protobuf `FilterRequest`

## Structure utile

- Java principal: `java-project/src/main/java/com/project/Main.java`
- Service SQL/affichage: `java-project/src/main/java/com/project/ReportService.java`
- Tables virtuelles H2: `java-project/src/main/java/com/project/H2Database.java`
- Pont Python: `java-project/src/main/java/com/project/PythonBridge.java`
- Script Python Numpy: `java-project/src/main/python/std.py`
- JNI natif C++: `cpp-project/bridge.cpp`
- Lanceur C++ -> JVM: `cpp-project/main.cpp`
- Protobuf source: `java-project/src/main/java/com/project/proto/filter.proto`
- Protobuf Java pregenere: `java-project/src/main/java/com/project/proto/FilterRequestOuterClass.java`
- Protobuf C++ pregeneres: `cpp-project/generated/filter.pb.h`, `cpp-project/generated/filter.pb.cc`

## Generation protobuf (obligatoire, version imposee)

Le sujet impose protobuf 5.29.3 (protoc `libprotoc 29.3`).

Depuis la racine du workspace:

```bash
./generate_proto.sh
```

Ou depuis le dossier du `.proto`:

```bash
cd java-project/src/main/java/com/project/proto
./generate_proto.sh
```

Les scripts verifient la version et echouent si `protoc --version` n'est pas `libprotoc 29.3`.

## Build Java

Depuis la racine:

```bash
mvn -f java-project/pom.xml clean package
```

Execution Java (Q2 + Q3):

```bash
mvn -f java-project/pom.xml -q exec:java
```

## Build/Run C++ (CMake + vcpkg)

Prerequis:

- `cmake`
- `ninja`
- `VCPKG_ROOT` configure

Configuration:

```bash
cmake -S cpp-project -B cpp-project/build -DCMAKE_TOOLCHAIN_FILE=$VCPKG_ROOT/scripts/buildsystems/vcpkg.cmake
```

Build:

```bash
cmake --build cpp-project/build -j
```

Run:

```bash
./cpp-project/build/cpp_interop
```

## Execution via boutons VS Code

Le dossier `.vscode/` contient:

- `tasks.json`: build Java, configure/build/run C++
- `launch.json`: lancement Java et C++

Utiliser ces configurations pour rester conforme a la correction en TP.

## Remise Moodle

Avant de zipper:

- Verifier dans un workspace vierge
- Conserver uniquement les sources et configurations utiles
- Ne pas inclure `CMakeUserPresets.json` (s'il existe localement)
- Ajouter a cote du dossier `Workspace` un fichier `.txt` avec les noms du binome/trinome
- Utiliser uniquement des chemins relatifs compatibles Linux
