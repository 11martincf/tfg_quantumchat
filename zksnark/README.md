# ZKSNARK: Guía de usuario

Este documento es una explicación general del proyecto de NFC + zk-SNARK. 

La sección de Instalación explica cómo configurar los programas para que se ejecuten correctamente.

## Arquitectura

El proyecto zk-SNARK está compuesto por los siguientes programas:

- ZK-Client: Proyecto en Java para el cliente. Envía su certificado al servidor y genera la prueba zk-SNARK.
- ZK-Server: Proyecto en Java para el servidor. Utiliza Spring Boot para habilitar una API REST desde la que recibe peticiones de los usuarios: autenticación con certificado, validación de pruebas... Tiene acceso al QRNG.
- ZK-Validator: Proyecto en Java para el validador de pruebas. Similar al servidor, pero solo recibe pruebas zk-SNARK a través del lector NFC (utiliza el proyecto NFC-Reader). Muestra una GUI con información sobre la validación de las pruebas.

### Clases comunes

El paquete "model" es común a los 3 proyectos. Cada programa contiene una copia, así que **cada modificación debe hacerse en los 3 proyectos**. Este paquete contiene la siguiente información:

- Constantes: fichero para configurar nombres por defecto, rutas y endpoints
- Clases que representan estructuras de datos: _MerkleTree_, _NullifierList_, _ZKProof_, _LibsnarkProof_
- Mensajes: encapsulan mensajes entre el cliente y los servidores. Se serializan para enviarlos como texto.

### Cliente

La clase principal con la que interactuar es _ProofClient_. Esta clase se encarga de la generación de zk-SNARKs y de enviarlos al servidor. Un ejemplo de ejecución es:
```
public static void main(String[] args) {
    // Inicializar con la URL del servidor y la carpeta de instalación
    ProofClient proofClient = new ProofClient("http://localhost:8080", Constants.DEFAULT_CLIENT_FOLDER);
    proofClient.init();
    try {
        // Autenticación con certificado
        proofClient.doAuthenticate("alice.p12");
        // Descargar Árbol Merkle
        proofClient.downloadMerkleTree();
        // Cargar información
        MerkleTree merkleTree = proofClient.loadMerkleTree();
        CommitNote commitNote = proofClient.loadCommitNote();
        // Generar y enviar prueba
        proofClient.generateZokratesProof(merkleTree, commitNote);
        ZKProof proof = proofClient.loadProof();
        //Recibir clave QRNG
        byte[] qrngKey = proofClient.sendZokratesProof(proof, 32);
        System.out.println(Base64.getEncoder().encodeToString(qrngKey));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

NOTA: El código del cliente está pensado para ejecutarse en un PC. Con suerte, podremos incorporar un .jar en la aplicación móvil y hacer llamadas a la clase _ProofClient_. Si no es posible, habrá que rehacer en la aplicación móvil la parte de autenticación con certificado y encontrar la manera de generar las pruebas zk-SNARK fuera de la aplicación.

### Servidor

En el protocolo original, el servidor se ocupa únicamente de la autenticación mientras que la validación de pruebas la hace un programa diferente por NFC. Para simplificar el proceso, en esta implementación el servidor también puede recibir las pruebas por HTTP y validarlas. Por lo tanto, algunas clases que deberían estar solo en el Validador (_CommandHandler_, acceso a los QRNG) se encuentran también en este Servidor.

El servidor define los endpoints en los que escucha peticiones HTTP en las siguientes clases:

- _MerkleTreeController_: "/tree/download" para descargar el árbol Merkle, "/tree/roots" para obtener la lista de antiguos roots
- _SnarkController_: "/commit" para autenticación, "/nullifiers" para obtener la lista de nullifiers, "/proof" y "/rawProof" para enviar prueba de ZoKrates o Libsnark, respectivamente (leer siguiente apartado). 

El cliente tiene configurados estos endpoints en la clase _Constants_. Se puede comprobar con el navegador que el servidor devuelve correctamente la información en formato JSON.

### Validador

El Validador utiliza un lector NFC para recibir pruebas zk-SNARK y las valida. En la versión actual, el Validador solo puede interpretar pruebas de ZoKrates.

El programa tiene una GUI que muestra información sobre las pruebas que recibe, mostrando el root y el nullifier entre otra información. Para realizar pruebas se recomienda desactivar la comprobación de que el nullifier es único, de forma que se pueda presentar varias veces la misma prueba.
La GUI puede dar problemas al ejecutar el proyecto desde Intellij, ya que puede que no genere automáticamente el código de GUI. Si esto ocurre, contacta conmigo.

## Backends

Estas dos librerías (de ahora en adelante, _zk-SNARK backends_ o simplemente _backends_) se encargan de la generación y verificación de zk-SNARKs. Ambas están integradas en ZK-Client y ZK-Server, **pero ZK-Validator solo utiliza ZoKrates**. Los programas se comunican con el backend a través de la línea de comandos en la clase _CommandHandler_, que utiliza la clase ProcessBuilder de Java para iniciar un proceso por línea de comandos. Se puede modificar esta clase para que imprima por pantalla el comando que va a ejecutar, lo cual es útil para familiarizarse con los comandos. 

La elección de ZoKrates o Libsnark es relativamente transparente: el cliente tiene funciones para interactuar con ZoKrates o Libsnark dependiendo de qué backend se quiera utilizar. **En caso de duda, se recomienda utilizar únicamente ZoKrates** ya que es más simple de instalar. la clase _ProofClient_ tiene diferentes comandos para utilizar los distintos backends. Además, la prueba se almacena de distinta manera.

Debido a que estos programas requieren saber el tamaño de ciertos arrays en tiempo de compilación, modificar la profundidad del Merkle Tree no es sencillo y requiere volver a compilar los programas. Se ha fijado esta variable a 15, permitiendo aproximadamente 4 millones de Commitments sin que el rendimiento se vea muy perjudicado. En principio no será necesario modificar esta variable por lo que no se tendrán que compilar los backends. En cualquier caso, las siguientes secciones indicarán cómo realizarlo.

### ZoKrates

La instalación de ZoKrates en Linux se puede hacer desde la línea de comandos. Consultar página: https://zokrates.github.io/ y repositorio: https://github.com/Zokrates/ZoKrates

Es importante diferenciar entre el ejecutable de ZoKrates propiamente dicho ("zokrates.exe") y el programa compilado de generación de pruebas ("MerkleTreeCommitment"). El primero es el proporcionado por los creadores de ZoKrates y no ha sido modificado, mientras que el segundo es el resultado de compilar el código "MerkleTreeCommitment.zok", creado específicamente para este proyecto. Siempre que se mencionen modificaciones al código o compilaciones, es con respecto a "MerkleTreeCommitment.zok" y no al código de la librería.

Por algún motivo, ZoKrates almacena las cadenas de 256 bits como una lista de 8 unsigned integers, cada uno de 32 bits. Prácticamente todos los inputs que queremos enviarle son cadenas de 256 bits, así que es necesario formatearlos de esa manera. Esto se hace en la clase _ZokratesProofGenerator_ del cliente, y puede comprobarse el resultado imprimiendo en _CommandHandler_ el comando a ejecutar.

Funciones de _ProofClient_ para utilizar ZoKrates:
```
// Generar y enviar prueba
proofClient.generateZokratesProof(merkleTree, commitNote);
ZKProof proof = proofClient.loadProof();
//Recibir clave QRNG
byte[] qrngKey = proofClient.sendZokratesProof(proof, 32);
```

ZoKrates almacena la prueba generada en el fichero "proof.json", que contiene el root y nullifier. El JSON se deserializa en la clase _ZKProof_ mediante la función "loadProof".

Comandos de ZoKrates:
- Compilación: 
```
.\zokrates.exe compile -i MerkleTreeCommitment.zok -o MerkleTreeCommitment
```
- Creación de claves de prueba y verificación: 
```
.\zokrates.exe setup -i MerkleTreeCommitment
```
- Generación de pruebas: debe generarse primero el witness y después la prueba.
```
.\zokrates.exe compute-witness -i MerkleTreeCommitment -a 3074700584 3297633031 3586955801 596102331 2510301539 3541234568 4192264471 261357471 3694357574 3209696115 2744578102 3444621100 3748548492 507865226 520184419 489593409 4274748281 1987463201 838268950 3895429365 394107150 2819274376 3720704477 2356102804 2 124397511 2656751146 1028127197 780395775 1856538411 1008867574 1821554843 1442014560 1551731367 3990524871 1287917505 3067797748 173343371 2369310072 4076470279 713754573 44761127 3767745481 456920052 4258932622 3113558012 2143645027 2542720859 956652685 192581672 463918470 3039789661 2897768309 23264053 1630770507 2203458128 2618280509 1919659818 855723775 1228099537 1576734779 2732467002 1563540401 3853004647 1480635276 1570800242 2946367298 2400415195 30270881 2958269891 2974649601 95341755 162131955 3458954803 3129621389 3660766988 1059436805 1164907141 1682791105 2804186849 2736952205 1536416055 1661868864 2437401428 25615811 598517623 1000019315 3826150235 2120092690 2491448803 1556518222 2832927532 2989494499 3771074425 18599028 1246886353 757211706 2081219313 2585321247 344997021 261034445 1503059972 2586456332 105003828 1134462869 1568973916 900247223 2680031666 3005138058 4070577543 2966166433 397131196 4017998637 609859258 3824102339 3880320535 1208493104 780848012 2747072069 1424675035 3080066725 2781632314 2525761659 1482129315 964149825 137221024 1602659599 29188632 112002440 174147161 2426679788 4227565212 1583043394 3320064619 3128866329 3246823852 1626001575 2026402776 3790187636 1125066562 1866574701 4142449049 3529206398 4057857133 2594476076 3257770671 1126241521 3164731293 2654330723 418790464 954852372 1680176805 3589928573 1131353510 4075607393 2397264447 832811729 2604816875 1933262996 4080691625 2553500063 3363759428 2530306752 3316374645 965951524 3763480610 1379792578 3771605124 1667105125

.\zokrates.exe generate-proof -i MerkleTreeCommitment
```
NOTA: Como se ha mencionado, los parámetros deben introducirse como unsigned integers. Excepto "comIndex" que es un entero, cada cadena de 256 bits corresponde a 8 números. El orden de los parámetros que se introducen por línea de comandos es el especificado en la función 'main': root -> nullifier -> commitment -> comIndex -> path[0] -> ... -> path[N] -> pubKey -> privKey -> sigma. En caso de dudas, consultar "MerkleTreeCommitment.zok".

- Validación de pruebas:
```
.\zokrates.exe verify -j proof.json
```

### Libsnark

Se proporcionará el código compilado de manera que no sea necesario tratar directamente con él. Si se requiere analizar, recompilar o modificar el código, consultar repositorio: https://github.com/scipr-lab/libsnark y tutorial de instalación: https://github.com/howardwu/libsnark-tutorial 

Libsnark en Windows se ejecuta a través de Cygwin, por lo que es necesario instalarlo junto con una serie de dependencias (cmake, libgmp, g++, libssl, libcrypto, git, boost...). El repositorio de libsnark solo indica "g++, libgmp, cmake, git", pero el resto de las indicadas también son necesarias (por lo menos para la compilación, probablemente también para la ejecución). La ejecución de Libsnark también debe realizarse a través de Cygwin. Debido a esto, por línea de comandos debe indicarse lo siguiente:

```
C:\<Cygwin_path>\bin\bash --login -c "cd /cygdrive/c/<path_carpeta_instalacion>; ./libsnark.exe <argumentos>
```

Se recomienda el uso de CLion para tratar con este código, ya que puede configurarse para utilizar Cygwin como toolchain en File -> Settings -> Build, Execution, Deployment -> Toolchains. El código consiste en un proyecto en C++ que utiliza CMake para la compilación. En la carpeta "depends" se incluye el código de libsnark y otras dependencias que se requieran. El código propio de este proyecto se encuentra en los ficheros "main" y "merklecircuit.h". Además, "sha256_extragadget.hpp" es una pequeña modificación del gadget de SHA-256 para que aplique el padding estándar. Si tienes problemas compilando el código, contacta conmigo.

Como siempre, consultar la salida de _CommandHandler_ para un ejemplo de cómo ejecutar libsnark.

NOTA: Actualmente Libsnark solo funciona en Windows, debido a que es la plataforma que yo utilizo. Si necesitas utilizar Linux y libsnark, contacta conmigo. 

Funciones de _ProofClient_ para utilizar Libsnark:
```
// Generar y enviar prueba
LibsnarkProof proof = proofClient.generateLibsnarkProof(merkleTree, commitNote);
//Recibir clave QRNG
byte[] qrngKey = proofClient.sendLibsnarkProof(proof, 32);
```

Comandos de Libsnark:
- Creación de claves de prueba y verificación: 
```
C:\<Cygwin_path>\bin\bash --login -c "cd /cygdrive/c/<path_carpeta_instalacion>; ./libsnark.exe setup
```
- Creación de pruebas:  
```
C:\<Cygwin_path>\bin\bash --login -c "cd /cygdrive/c/<path_carpeta_instalacion>; ./libsnark.exe prove 085f706db972e1d1802f95e0aea2b8c5d016c6d23d7f2e29b4ae709676fe7be5 ce6e398c90acafa8c75a005805c67a732163ca150cb14d8e62685f281eac311e 9cc1c35985a59f2eb174d6d23a65ab7c1c41bc4dbec05db874d98f826d8e3cf2 1e1c78142e04ceb426b149ea12e18ad29ac074677dc617d7941d07bd8ddd4f5f fa22b6f4ee8c40744f958930f267a0e0192f6eb9c0f134424d079fbb48ce2a89 8ce2f4c642eb4d9d191b509d486fae4982f26caf308e0ffbc09789d061ec2b04 2 076a27c79e5ace2a3d47f9dd2e83e4ff6ea8872b3c2218f66c92b89b55f36560 bf90f81908167b8f620d6a87ba148b452a47fca5e0def86dda75ba05cb3ba676 02ab0027e0933bc91b3c0bf4fdda278eb9951ffc7fc56d63978ed75b39055c8d 0b7a90281ba6d586b52f825dacb86f750162fb356133954b83561a509c0fca3d 726baf2a33014eff49334fd15dfb103ba2de233a5d31bbb1e5a82f675840b38c 5da08272af9dff428f136ddb01cde5a1b0539dc3b14d8d0105aeccbb09a9eff3 ce2b7633ba8a3b8dda32df0c3f25b905456f1285644d5ac1a7247ee1a322938d 5b93d937630e1b409147cb540186ddc323aca7773b9b1573e40e6b5b7e5e0c12 94807de35cc6954ea8db0b2cb23010e3e0c60779011bcc744a51f9d12d22223a 7c0ce2f19a18df1f14903c9d0f8f11cd5996e0049a2a310c06423b34439e8795 5d84a45c35a8aeb79fbe09b2b31ec48af2a01587b0cc1ba117abbdbcef7dcb2d 2459b6bae3ef2bc3e748fe17480824302e8acb8ca3bcfe4554ead0dbb79616a5 a5cc573a968c107b58577fa33977c241082dd3a05f86a50f01bd621806ad0588 0a61465990a431ecfbfb869c5e5b5342c5e42a6bba7eb619c18699ac60ead0a7 78c873d8e1e9ac74430f27426f41ab6df6e8c199d25b6a7ef1ddfc6d9aa4902c
```
NOTA: Las cadenas de 256 bits se representan en hexadecimal. El orden de los parámetros que se introducen por línea de comandos es: root -> nullifier -> commitment -> pubKey -> privKey -> sigma -> comIndex -> path[0] -> ... -> path[N] (**El orden es distinto que con ZoKrates**). 

- Creación de claves de prueba y verificación: 
```
C:\<Cygwin_path>\bin\bash --login -c "cd /cygdrive/c/<path_carpeta_instalacion>; ./libsnark.exe verify verify proof.raw 085f706db972e1d1802f95e0aea2b8c5d016c6d23d7f2e29b4ae709676fe7be5 ce6e398c90acafa8c75a005805c67a732163ca150cb14d8e62685f281eac311e
```
NOTA: Al contrario que con ZoKrates, es necesario pasar por línea de comandos el root y nullifier para la verificación.

## Instalación

NOTA: esta sección menciona requisitos de ZoKrates y Libsnark. Si solo se planea utilizar uno de los dos backends, los requisitos del otro backend pueden ignorarse.

Existen dos carpetas de instalación que contienen información necesaria para la ejecución de los programas, y que permiten almacenar los ficheros que se generan durante la ejecución:
- zkclient: Carpeta del **Cliente**
- zkserver: Carpeta del **Servidor** y del **Validador**

**Es necesario modificar la clase _Constants_ de los 3 proyectos para indicar la ubicación de estas carpetas**. 

### Ficheros de instalación

Los siguientes ficheros son obligatorios y deben existir antes de ejecutar el programa para que funcione correctamente:

- Programas .exe para ejecutar ZoKrates y Libsnark (Cliente + Servidor). Los nombres por defecto son "zokrates.exe" y "libsnark.exe", pero se pueden modificar en la clase _Constants_.
- Código compilado de ZoKrates (Cliente + Servidor). Se llama MerkleTreeCommitment. El código fuente "MerkleTreeCommitment.zok" se incluye como referencia pero no es necesario. Pesa 1-2 GB así que se puede generar directamente en la carpeta de instalación a partir del código fuente. Otros ficheros como "abi.json" y "out.r1cs" se generan también en la compilación, así que también deben estar en la carpeta.
- Claves de prueba para ZoKrates y Libsnark (Cliente). Se llaman "proving.key" y "merkle_pk.raw", respectivamente. Se generan con los comandos de generación indicados en el apartado anterior. No modificar el nombre.
- Claves de verificación para ZoKrates y Libsnark (Servidor). Se llaman "verification.key" y "merkle_vk.raw", respectivamente. Se generan con los comandos de generación indicados en el apartado anterior. No modificar el nombre.
- Carpeta con certificados (Cliente): almacenar aquí los certificados que serán utilizados para autenticación. NOTA: estaría bien modificar la función que carga los certificados para que reciba una ruta absoluta, ya que ahora recibe una ruta relativa desde "<carpeta_instalacion>/certs".

### Ficheros generados

Los siguientes ficheros se generan durante la ejecución:
- Commit Note (Cliente): JSON serializado de la clase CommitNote. Almacena las claves pública y privada y Sigma, que sirven para generar el Commitment y Nullifier. Nombre: "commit.json"
- Árbol Merkle. Nombre: "tree.raw". NOTA: antes el fichero era un JSON, mientras que ahora es un archivo binario. Si se encuentra alguna referencia a "tree.json", modificar.
    - Cliente: Lo descarga del servidor para generar la prueba zk-SNARK. No es necesario mantenerlo para posteriores ejecuciones.
    - Servidor: Almacena el árbol para que pueda ser cargado en ejecuciones posteriores. Si no existe al iniciar el programa, crea uno vacío. Lo guarda en el fichero cada vez que hay una modificación.
- Lista de nullifiers (Servidor): Lista de todos los nullifiers ya publicados. Ai no existe al iniciar el programa, crea uno vacío. Lo guarda en el fichero cada vez que hay una modificación. Nombre: "nullifiers.json".
- Prueba generada por el zk-SNARK backend. Las pruebas generadas por ZoKrates y Libsnark son diferentes y no son compatibles.
    - Cliente: Las genera durante la ejecución. Con ZoKrates se llama "proof.json" e incluye el Root y el Nullifier en la prueba, mientras que con Libsnark deben enviarse aparte (revisar sección "ZoKrates y Libsnark"). 
    - Servidor: Recibe la prueba a través del endpoint "/proof" of "/rawProof". Las almacena en la carpeta "proofs/" y les asigna como nombre el Nullifier en hexadecimal seguido de ".json" o ".raw".

Las clases que se encargan de escribir información en estos ficheros son _FileDAO_ en el caso del Cliente, _FileDAO_ y _FileAccessor_ en el caso del servidor y _LocalValidatorDAO_ y _FileAccessor_ para el Validador. se utiliza Gson para serializar las clases a JSON o, en el caso de los ficheros binarios, ObjectOutputStream. NOTA: a pesar de que estas clases se llamen de forma muy similar, **tienen diferencias importantes**, especialmente en cómo gestionan la construcción de las rutas absolutas.
