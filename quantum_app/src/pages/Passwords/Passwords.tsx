import React, { useState, useEffect } from 'react';
import {
  IonContent,
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonItem,
  IonLabel,
  IonButton,
  IonText,
  IonButtons,
  IonIcon,
  IonMenuToggle,
  IonSpinner,
} from '@ionic/react';
import forge from 'node-forge';
import { folderOpen, menu } from 'ionicons/icons';
import CommitNote from '../../models/CommitNote';
import Constants from '../../models/Constants';
import { Storage } from '@ionic/storage';
import { HashUtil } from '../../models/HashUtil';
import CommitNoteMessage from '../../models/CommitNoteMessage';

const Passwords: React.FC = () => {

  const [storage, setStorage] = useState<Storage | null>(null);

  const [isStorageInitialized, setIsStorageInitialized] = useState(false);

  const [step, setStep] = useState(1);

  const [inputValues, setInputValues] = useState({
    step1: '',
    step2: '',
    step3: '',
    step4: '',
    selectedFile: null,
    fileName: ''
  });

  useEffect(() => {
      async function initializeStorage() {
          const storageInstance = new Storage();
          await storageInstance.create();
          setStorage(storageInstance);
          setIsStorageInitialized(true);
      }
      initializeStorage();
  }, []);

  useEffect(() => {
    if (step === 2) {
        outsource();
    } else if (step === 3) {
        sendZokratesProof();
    }
}, [step]);

  const [password, setPassword] = useState('');

  const handleFileSelect = (e: any) => {
    setInputValues({ 
        ...inputValues, 
        selectedFile: e.target.files[0],
        fileName: e.target.files[0]?.name 
    });
  };

  let cert = '';

  let privateKey = '';

  const [commitNote, setCommitNote] = useState<CommitNote | null>(null);

  const [proof, setProof] = useState('');
  
  function uint8ArrayToBase64(array: Uint8Array): string {
    let binaryString = '';
    for(let i = 0; i < array.byteLength; i++) {
        binaryString += String.fromCharCode(array[i]);
    }
    return btoa(binaryString);
}

  const readFile = (file: Blob): Promise<void> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();

        reader.onload = async (e) => {
            if (inputValues.fileName.endsWith('.p12')) {
              const p12Der = forge.util.decode64(btoa(e.target!.result as string));
              const p12Asn1 = forge.asn1.fromDer(p12Der);

              const p12 = forge.pkcs12.pkcs12FromAsn1(p12Asn1, '');

              const bags = p12.getBags({localKeyIdHex: '0BD76529DCA7760DAD02522ACA4F12540F7FC76C'});
              const certBag = bags.localKeyId![0];
              const privateKeyBag = bags.localKeyId![1];
              console.log('bags:', bags);

            cert = forge.pki.certificateToPem(certBag.cert!);
            privateKey = forge.pki.privateKeyToPem(privateKeyBag.key!);

            } else if (inputValues.fileName.endsWith('.pem')){



            }

            resolve();
            
        };

        reader.onerror = (error) => {
            reject(error);
        };

        reader.readAsBinaryString(file);
    });
  };


  const createCommitNote = async () => {
    try {
         // Usar el API Web Crypto para generar valores aleatorios seguros
         const sigma = window.crypto.getRandomValues(new Uint8Array(Constants.KEYPAIR_SIZE));
         const privateKeyBytes = window.crypto.getRandomValues(new Uint8Array(Constants.KEYPAIR_SIZE));
 
         // Crear el commit note
         const commitNote = new CommitNote(sigma); // Modificado aquí

         await storage!.set(Constants.DEFAULT_COMMIT_FILENAME, JSON.stringify(commitNote));
 
         return commitNote;

    } catch (e) {
        console.error('Error creating commit note:', e);
        throw e;
    }
  };

  const doAuthenticate = async () => {

    try {

      await readFile(inputValues.selectedFile!);

      console.log('certPem:', cert);
      console.log('privateKeyPem:', privateKey);        

      // Se crea el CommitNote
      const commitNote = await createCommitNote();

      // Se crea el digest
      const digest = HashUtil.generateCommitment(commitNote);

      setCommitNote(commitNote);

      // Se firma el digest con la clave privada
      const signedCommit = HashUtil.signCommitment(digest, forge.pki.privateKeyFromPem(privateKey));

      // Se codifica en base64
      const encodedSign = uint8ArrayToBase64(signedCommit);
      const encodedCert = btoa(cert!); // Asumiendo que cert ya es una cadena
      const encodedCommit = uint8ArrayToBase64(digest);

      console.log('encodedSign:', encodedSign);
      console.log('encodedCert:', encodedCert);
      console.log('encodedSignedCommit:', encodedCommit);

      // Se crea el commitMessage
      const commitMessage = {
        encodedCommit: encodedCommit, 
        encodedCert: encodedCert,
        encodedSign: encodedSign,
      };

      // Enviamos este mensaje al servidor
      const response = await fetch("http://localhost:8080/commit", {
        method: "POST",
        body: JSON.stringify(commitMessage),
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('response:', response);

      if (!response.ok) {
        // Gestionar error (mostrar un mensaje, por ejemplo)
        console.error(`Error en la solicitud: ${response.statusText}`);
        return;
      }

      const data = await response.json();
      console.log('data:', data);

      if (data.response === "true") {
        setStep(step + 1);
      } else {
        // Gestionar error (mostrar un mensaje, por ejemplo)
      }
    } catch (error) {
      // Gestionar errores de red o de otra índole
      console.error("Ha ocurrido un error:", JSON.stringify(error, null, 2));
    }
    
  };

  const outsource = async () => {

    // SE SOLICITA LA PRUEBA DEL OUTSOURCE MIENTRAS LA APP ESPERA

    // Se crea el CommitNoteMessage
    const commitNoteMessage = new CommitNoteMessage(commitNote!);

    console.log('commitNoteMessage:', commitNoteMessage);

    try {
      const proofResponse = await fetch("http://localhost:8080/outsource", {
        method: "POST",
        body: JSON.stringify(commitNoteMessage),
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!proofResponse.ok) {
        // Gestionar error (mostrar un mensaje, por ejemplo)
        console.error(`Error en la solicitud: ${proofResponse.statusText}`);
        return;
      }

      console.log('proofResponse:', proofResponse);

      const proofData = await proofResponse.json();

      // Cambiar el valor de keySize a 32
      if (proofData && proofData.keySize !== undefined) {
        proofData.keySize = 32;
      }

      setProof(proofData);

      console.log('proofData:', proofData);
      
      if (proofData != null) {
        setStep(step + 1);
      } else {
        // Gestionar error (mostrar un mensaje, por ejemplo)
      }
      
    } catch (error) {
      // Gestionar errores durante la solicitud de la prueba
      console.error("Error al obtener la prueba:", error);
    }

  }

  const sendZokratesProof = async () => {

    try {
      const response = await fetch("http://localhost:8080/proof", {
        method: "POST",
        body: JSON.stringify(proof),
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('response:', response);
    
      if (!response.ok) {
        throw new Error('Network response was not ok ' + response.statusText);
      }
    
      const responseBody = await response.text();
      const sealedKey = Uint8Array.from(atob(responseBody), c => c.charCodeAt(0));
      const sealedKeyBase64 = uint8ArrayToBase64(sealedKey);
      console.log('sealedKeyBase64:', sealedKeyBase64);

      getPassword(sealedKeyBase64);

    } catch (error) {
      console.error('There has been a problem with your fetch operation:', error);
    }
    

  }

  const generatePrivateKey = async (privKeyBytes: BufferSource) => {
    try {
      const key = await crypto.subtle.importKey(
        "pkcs8",
        privKeyBytes,
        {
          name: "RSA-OAEP",
          hash: "SHA-256"
        },
        true,
        ["decrypt"]
      );
      return key;
    } catch (error) {

    }
  }
/*
  const decryptData = async (privateKey: CryptoKey, sealedKey: BufferSource | Iterable<number>) => {
    try {
      // Suponiendo que sealedKey es un Uint8Array y contiene los datos encriptados
      const unsealedKey = await crypto.subtle.decrypt(
        {
          name: "RSA-OAEP",
          // Puedes necesitar establecer otros parámetros dependiendo de tus requisitos
        },
        privateKey, // La clave privada que has importado o generado previamente
        sealedKey // Los datos encriptados que quieres desencriptar
      );
      
      console.log("SEALED:", btoa(String.fromCharCode(...new Uint8Array(sealedKey))));
      console.log("UNSEALED:", btoa(String.fromCharCode(...new Uint8Array(unsealedKey))));
      
      return unsealedKey;
    } catch (error) {
      console.error("Error al desencriptar los datos:", error);
    }
  }*/
  
  function getPassword(sealedKey: string | Iterable<number>) {

    const privateKeyBytes = commitNote!.getEncodedPrivateKey();

    generatePrivateKey(privateKeyBytes!)
    .then(privateKey => {
      console.log("Clave privada generada:", privateKey);
    })
    .catch(error => {
      console.error("Error al generar la clave privada:", error);
    });
/*
    const sealedKeyUint8Array = new Uint8Array(sealedKey);
    const sealedKeyBuffer = sealedKeyUint8Array.buffer;

    decryptData(privateKey, sealedKeyBuffer)
      .then(unsealedKey => {
        // Haz lo que necesites con unsealedKey aquí
      })
      .catch(error => {
        console.error("Error al desencriptar los datos:", error);
      });



    setPassword(decryptedPassword);
    setStep(step + 1);
*/
  }

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonMenuToggle>
              <IonButton>
                <IonIcon slot="icon-only" icon={menu}></IonIcon>
              </IonButton>
            </IonMenuToggle>
          </IonButtons>
          <IonTitle>Contraseñas</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">

        <h1>Obtener contraseña</h1>
        <IonLabel position="floating">Paso {step} de 4</IonLabel>

        {step === 1 && (
          <>
          <input
            type="file"
            accept=".csr, .p12, .pem, .srl" // Tipos de archivos aceptados
            onChange={handleFileSelect}
            style={{ display: 'none' }}
            id="file-input"
            />
            <IonItem>
              <IonLabel>{inputValues.fileName ? inputValues.fileName : 'Elegir archivo'}</IonLabel>
              <IonButtons slot="end">
                  <IonButton
                  fill="clear"
                  onClick={() => {
                      document.getElementById('file-input')?.click();
                  }}
                  >
                  <IonIcon icon={folderOpen} />
                  </IonButton>
              </IonButtons>
            </IonItem>
            <IonButton expand="block" onClick={doAuthenticate} disabled={!isStorageInitialized || (step === 1 && !inputValues.selectedFile)}>
                Enviar
            </IonButton>
          </>
        )}

        {step === 2 && (
          <>
          <IonItem>
            <IonLabel>Generando prueba</IonLabel>
            <IonSpinner name="crescent"></IonSpinner>
          </IonItem>
        </>
        )}

        {step === 3 && (
          <>
            <IonItem>
              <IonLabel>Prueba zk-Snark obtenida</IonLabel>
            </IonItem>
            <IonItem>
              <IonLabel>
                Acércate a un punto de validación para validar tu prueba
                por NFC y obtener tu contraseña
              </IonLabel>
            </IonItem>
          </>
        )}

        {step === 4 && (
          <>
            <IonText>
              <h1>Tu contraseña es:</h1>
              <p>{password}</p>
            </IonText>
            <IonButton expand="block" onClick={() => setStep(1)}>
              Reiniciar
            </IonButton>
          </>
        )}
      </IonContent>
    </IonPage>
  );
};

export default Passwords;

