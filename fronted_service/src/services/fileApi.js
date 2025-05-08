import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:1050/api/archivos',
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const subirArchivo = (formData) => {
    return api.post('/subir', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        }
    });
};

export const listarArchivos = () => {
    return api.get();
};

export const renombrarArchivo = (id, nombre) => {
    return api.patch(`/${id}/renombrar`, { nombre });
};

export const descargarArchivo = (id) => {
    return api.get(`/${id}/descargar`, {
        responseType: 'blob',
    });
};

export const cambiarEstadoArchivo = (id) => {
    return api.patch(`/${id}/estado`);
};
