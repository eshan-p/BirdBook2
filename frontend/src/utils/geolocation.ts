import { Point } from "geojson";

export interface Coordinates {
    latitude: number;
    longitude: number;
}

//Format fields
export const pointToCoords = (point: Point): Coordinates => {
    const [longitude, latitude] = point.coordinates;
    return {latitude, longitude};
};

//Convert coords to GeoJSON point
export const coordsToPoint = (latitude: number, longitude: number): Point => {
    return {
        type: 'Point',
        coordinates: [longitude, latitude]
    }
}

//IMPORTANT: make sure it's in correct oder (lat, lon)
export const arrayToCoords = (arr: [number, number]): Coordinates => {
    return{
        longitude: arr[1],
        latitude: arr[0]
    }
}

//Format coords for display
export const formatCoords = (coords: Coordinates, decimals = 4): string => {
    return `${coords.latitude.toFixed(decimals)}, ${coords.longitude.toFixed(decimals)}`;
}


export const reverseCoordsToCityState = async (coords: Coordinates): Promise<string> => {
    const latitude = coords.latitude;
    const longitude = coords.longitude;
    try{
        console.log(latitude, longitude)
        const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${latitude}&lon=${longitude}`)
        const data = await response.json();
        console.log(data)
        const city = data.address.city || data.address.town || data.address.village || data.address.hamlet || data.address.county;;
        const state = data.address.state;
        if(city && state){
            return `${city}, ${state}`;
        }else{
            return formatCoords(coords);
        }
    }catch(err){
        console.error("Error reversing Geo Code: " + err);
        return formatCoords(coords)
    }
}

export const reverseCoordsToRegion = async (coords: Coordinates): Promise<string> => {
    const latitude = coords.latitude;
    const longitude = coords.longitude;
    try{
        const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${latitude}&lon=${longitude}`)
        const data = await response.json();
        const continent = data.address.continent;
        if(continent){
            return `${continent}`;
        }else{
            return "Unknown";
        }
    }catch(err){
        console.error("Error reversing Geo Code: " + err);
        return formatCoords(coords)
    }
}