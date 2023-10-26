import CommitNote from "./CommitNote";

class CommitNoteMessage {

    private encodedPublicKey: string;
    private encodedPrivateKey: string;
    private encodedSigma: string;

    constructor(commitNote: CommitNote) {
        this.encodedPrivateKey = commitNote.getEncodedPrivateKey() 
            ? uint8ArrayToBase64(commitNote.getEncodedPrivateKey()!) 
            : "";

        this.encodedPublicKey = commitNote.getEncodedPublicKey() 
            ? uint8ArrayToBase64(commitNote.getEncodedPublicKey()!) 
            : "";

        this.encodedSigma = commitNote.getSigma() 
            ? uint8ArrayToBase64(commitNote.getSigma()!) 
            : "";

    }

    public getEncodedPublicKey(): string {
        return this.encodedPublicKey;
    }

    public setEncodedPublicKey(encodedPublicKey: string): void {
        this.encodedPublicKey = encodedPublicKey;
    }

    public getEncodedPrivateKey(): string {
        return this.encodedPrivateKey;
    }

    public setEncodedPrivateKey(encodedPrivateKey: string): void {
        this.encodedPrivateKey = encodedPrivateKey;
    }

    public getEncodedSigma(): string {
        return this.encodedSigma;
    }

    public setEncodedSigma(encodedSigma: string): void {
        this.encodedSigma = encodedSigma;
    }
    
}

function uint8ArrayToBase64(array: Uint8Array): string {
    let binaryString = '';
    for(let i = 0; i < array.byteLength; i++) {
        binaryString += String.fromCharCode(array[i]);
    }
    return btoa(binaryString);
}

export default CommitNoteMessage;