import React from 'react'
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { Post } from '../../types/Post';
import 'leaflet/dist/leaflet.css';

function MapView({posts} : {posts: Post[]}) {
    const postsWithLocation = posts.filter(p => {
        const hasTags = p.tags?.latitude && p.tags?.longitude;
        return hasTags;
    });

    const center: [number, number] = postsWithLocation.length > 0 ? 
    [
        postsWithLocation.reduce((sum, p) => sum + parseFloat(p.tags?.latitude || '0'), 0) / postsWithLocation.length,
        postsWithLocation.reduce((sum, p) => sum + parseFloat(p.tags?.longitude || '0'), 0) / postsWithLocation.length
    ]
    : [33.2148, -96.8158];

    return (
        <MapContainer center={center} zoom={9} style={{height: '400px', width: '100%'}}>
            <TileLayer 
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; OpenStreetMap contributors'
            /> 
            {postsWithLocation.map((post) => (
                <Marker 
                    key={post.id} 
                    position={[
                        parseFloat(post.tags!.latitude!),
                        parseFloat(post.tags!.longitude!)
                    ]}
                >
                    <Popup>
                        <div className='text-sm'>
                            <p className='font-semibold'>{post.header}</p>
                            <p className='text-xs text-gray-600'>{post.bird || 'Unknown bird'}</p>
                        </div>
                    </Popup>
                </Marker>
            ))}
        </MapContainer>
    )
}

export default MapView