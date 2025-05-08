import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User } from 'lucide-react';
import { registerUser } from '../services/authApi.js';

function Register() {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: ''
    });
    const [errors, setErrors] = useState({});
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    };

    const validateForm = () => {
        const newErrors = {};

        // Validación del nombre
        if (!formData.username) {
            newErrors.username = "El nombre es obligatorio";
        }

        // Validación del email
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!formData.email) {
            newErrors.email = "El correo electrónico es obligatorio";
        } else if (!emailRegex.test(formData.email)) {
            newErrors.email = "El correo electrónico no es válido";
        }

        if (!formData.password) {
            newErrors.password = "La contraseña es obligatoria";
        } else if (formData.password.length < 8) {
            newErrors.password = "La contraseña debe tener al menos 8 caracteres";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        try {
            const response = await registerUser(formData);

            const { accessToken, refreshToken } = response.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            navigate('/login');
        } catch (error) {
            console.error('Error en el registro:', error.response ? error.response.data : error.message);
            if (error.response) {
                setErrors({ submit: error.response.data.message });
            } else {
                setErrors({ submit: 'Error en la conexión con el servidor.' });
            }
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gradient-to-tr from-purple-400 via-violet-200 to-purple-300">
            <div className="bg-white/80 backdrop-blur-md p-8 rounded-2xl shadow-xl w-full max-w-md border border-violet-300">
                <h2 className="text-3xl font-bold text-center text-violet-700 mb-6">Crear cuenta</h2>
                <form className="space-y-5" onSubmit={handleSubmit}>
                    {/* Nombre */}
                    <div>
                        <label className="block text-sm font-medium text-violet-700 mb-1">Nombre</label>
                        <div className="relative">
                            <User className="absolute left-3 top-2.5 text-violet-500" size={20} />
                            <input
                                type="text"
                                placeholder="Tu nombre"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                className="pl-10 pr-4 py-2 w-full border border-violet-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-violet-400"
                                required
                            />
                        </div>
                        {errors.username && <p className="text-red-500 text-sm">{errors.username}</p>}
                    </div>

                    {/* Email */}
                    <div>
                        <label className="block text-sm font-medium text-violet-700 mb-1">Email</label>
                        <div className="relative">
                            <Mail className="absolute left-3 top-2.5 text-violet-500" size={20} />
                            <input
                                type="email"
                                placeholder="ejemplo@correo.com"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                className="pl-10 pr-4 py-2 w-full border border-violet-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-violet-400"
                                required
                            />
                        </div>
                        {errors.email && <p className="text-red-500 text-sm">{errors.email}</p>}
                    </div>

                    {/* Contraseña */}
                    <div>
                        <label className="block text-sm font-medium text-violet-700 mb-1">Contraseña</label>
                        <div className="relative">
                            <Lock className="absolute left-3 top-2.5 text-violet-500" size={20} />
                            <input
                                type="password"
                                placeholder="••••••••"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                className="pl-10 pr-4 py-2 w-full border border-violet-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-violet-400"
                                required
                            />
                        </div>
                        {errors.password && <p className="text-red-500 text-sm">{errors.password}</p>}
                    </div>

                    {/* Botón de registro */}
                    <button
                        type="submit"
                        className="w-full bg-violet-600 hover:bg-violet-700 text-white font-semibold py-2.5 rounded-lg transition duration-200"
                    >
                        Registrarse
                    </button>

                    {/* Mostrar errores globales */}
                    {errors.submit && <p className="text-red-500 text-sm text-center mt-4">{errors.submit}</p>}
                </form>

                {/* Enlace para ir al login */}
                <p className="mt-5 text-center text-sm text-violet-600">
                    ¿Ya tienes una cuenta?{' '}
                    <Link to="/login" className="text-violet-800 hover:underline font-medium">
                        Inicia sesión
                    </Link>
                </p>
            </div>
        </div>
    );
}

export default Register;
