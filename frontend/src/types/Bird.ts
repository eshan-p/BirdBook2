export interface Bird {
  id: string;          // string
  _id?: never;         // prevent accidental usage
  commonName: string;
  scientificName?: string;
  imageURL?: string; // Can be external URL (Wikipedia) or backend path
  location?: [number, number] | null;
}
