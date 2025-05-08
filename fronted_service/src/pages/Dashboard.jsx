import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
    subirArchivo,
    listarArchivos,
    renombrarArchivo,
    descargarArchivo,
    cambiarEstadoArchivo,
} from "../services/fileApi.js";
import {
    FaUpload,
    FaDownload,
    FaEdit,
    FaSyncAlt,
    FaCheckCircle,
    FaUserCircle,
} from "react-icons/fa";
import { toast, ToastContainer } from "react-toastify";

const Dashboard = () => {
    const [archivos, setArchivos] = useState([]);
    const [nuevoArchivo, setNuevoArchivo] = useState(null);
    const [nombresRenombrados, setNombresRenombrados] = useState({});
    const [loading, setLoading] = useState(false);
    const fileInputRef = useRef(null);
    const navigate = useNavigate();

    const fetchArchivos = async () => {
        try {
            const response = await listarArchivos();
            setArchivos(response.data);
        } catch (error) {
            console.error("Error al obtener los archivos:", error);
            if (error.response?.status === 401) {
                alert("Sesión expirada. Por favor inicia sesión de nuevo.");
                navigate("/login");
            }
        }
    };

    useEffect(() => {
        fetchArchivos();
    }, []);

    const handleFileChange = (e) => {
        setNuevoArchivo(e.target.files[0]);
    };

    const handleSubirArchivo = async () => {
        if (!nuevoArchivo) return;
        const formData = new FormData();
        formData.append("archivo", nuevoArchivo);
        setLoading(true);
        try {
            await subirArchivo(formData);
            toast.success(`Archivo "${nuevoArchivo.name}" subido exitosamente`, {
                position: "top-right",
                autoClose: 3000,
            });
            setNuevoArchivo(null);
            if (fileInputRef.current) {
                fileInputRef.current.value = "";
            }
            await fetchArchivos();
        } catch (error) {
            console.error("Error al subir el archivo:", error);
            if (error.response?.status === 401) navigate("/login");
            toast.error("Hubo un error al subir el archivo", {
                position: "top-right",
                autoClose: 3000,
            });
        } finally {
            setLoading(false);
        }
    };

    const handleRenombrar = async (id) => {
        const nuevoNombre = nombresRenombrados[id];
        if (!nuevoNombre) return;
        try {
            await renombrarArchivo(id, nuevoNombre);
            toast.success("Archivo renombrado exitosamente", {
                position: "top-right",
                autoClose: 3000,
            });
            setNombresRenombrados((prev) => ({ ...prev, [id]: "" }));
            await fetchArchivos();
        } catch (error) {
            console.error("Error al renombrar el archivo:", error);
            if (error.response?.status === 401) navigate("/login");
            toast.error("Hubo un error al renombrar el archivo", {
                position: "top-right",
                autoClose: 3000,
            });
        }
    };

    const handleDescargar = async (id) => {
        try {
            const response = await descargarArchivo(id);
            const blob = new Blob([response.data], { type: "application/zip" });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `archivo_${id}.zip`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            toast.success("Archivo descargado exitosamente", {
                position: "top-right",
                autoClose: 3000,
            });
        } catch (error) {
            console.error("Error al descargar el archivo:", error);
            if (error.response?.status === 401) navigate("/login");
            toast.error("Hubo un error al descargar el archivo", {
                position: "top-right",
                autoClose: 3000,
            });
        }
    };

    const handleProcesar = async (id) => {
        try {
            await cambiarEstadoArchivo(id);
            toast.success("Archivo procesado exitosamente", {
                position: "top-right",
                autoClose: 3000,
            });
            await fetchArchivos();
        } catch (error) {
            console.error("Error al procesar el archivo:", error);
            if (error.response?.status === 401) navigate("/login");
            toast.error("Hubo un error al procesar el archivo", {
                position: "top-right",
                autoClose: 3000,
            });
        }
    };

    return (
        <div className="min-h-screen bg-gray-100">
            {/* Navbar */}
            <nav className="bg-blue-600 p-4 flex justify-between items-center text-white">
                <h1 className="text-2xl font-semibold">Dashboard de Archivos</h1>
                <div className="flex items-center space-x-4">
                    <button className="flex items-center space-x-2">
                        <FaUserCircle size={24} />
                        <span>Perfil</span>
                    </button>
                </div>
            </nav>

            {/* Dashboard content */}
            <div className="max-w-6xl mx-auto p-8 bg-white shadow-md rounded-lg mt-6">
                <div className="mb-8">
                    <h2 className="text-xl font-semibold text-center mb-4">
                        Agregar Nuevo Archivo
                    </h2>
                    <div className="flex flex-col items-center space-y-4">
                        <label
                            htmlFor="fileInput"
                            className="cursor-pointer flex items-center space-x-2 bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium py-2 px-4 rounded-md shadow-sm transition"
                        >
                            <FaUpload className="text-blue-600" />
                            <span>Elegir archivo</span>
                        </label>
                        <input
                            id="fileInput"
                            type="file"
                            ref={fileInputRef}
                            onChange={handleFileChange}
                            className="hidden"
                        />
                        {nuevoArchivo && (
                            <p className="text-gray-700 text-sm">
                                Archivo seleccionado:{" "}
                                <span className="font-medium">{nuevoArchivo.name}</span>
                            </p>
                        )}
                        <button
                            onClick={handleSubirArchivo}
                            disabled={loading || !nuevoArchivo}
                            className="py-2 px-6 bg-blue-500 text-white text-sm font-semibold rounded-md hover:bg-blue-600 transition"
                        >
                            {loading ? "Subiendo..." : "Subir archivo"}
                        </button>
                    </div>
                </div>

                <div>
                    <h3 className="text-2xl font-semibold mb-6">Archivos subidos</h3>
                    <ul>
                        {archivos.map((archivo) => (
                            <li
                                key={archivo.id}
                                className="flex items-center justify-between p-5 border-b border-gray-300"
                            >
                                <div className="flex-1 overflow-hidden">
                                    <span
                                        className={`font-medium text-xl text-ellipsis overflow-hidden whitespace-nowrap ${
                                            archivo.estado === "PROCESADO"
                                                ? "text-green-500"
                                                : "text-red-500"
                                        }`}
                                        title={archivo.nombreActual || archivo.nombreOriginal}
                                    >
                                        {archivo.nombreActual || archivo.nombreOriginal}
                                    </span>
                                    <p
                                        className={`text-sm ${
                                            archivo.estado === "PROCESADO"
                                                ? "text-green-500"
                                                : "text-red-500"
                                        }`}
                                    >
                                        {archivo.estado === "PROCESADO"
                                            ? "Procesado"
                                            : "No procesado"}
                                    </p>
                                </div>
                                <div className="flex items-center space-x-3">
                                    <button
                                        onClick={() => handleProcesar(archivo.id)}
                                        className={`px-3 py-1 bg-blue-500 text-white rounded-md hover:bg-blue-600 text-sm ${
                                            archivo.estado === "PROCESADO"
                                                ? "opacity-50 cursor-not-allowed"
                                                : ""
                                        }`}
                                        disabled={archivo.estado === "PROCESADO"}
                                    >
                                        <FaSyncAlt className="inline" /> Procesar
                                    </button>
                                    <input
                                        type="text"
                                        value={nombresRenombrados[archivo.id] || ""}
                                        onChange={(e) =>
                                            setNombresRenombrados({
                                                ...nombresRenombrados,
                                                [archivo.id]: e.target.value,
                                            })
                                        }
                                        placeholder="Nuevo nombre"
                                        className="border border-gray-300 p-2 rounded-md w-72 text-lg"
                                        disabled={archivo.estado !== "PROCESADO"}
                                    />
                                    <button
                                        onClick={() => handleRenombrar(archivo.id)}
                                        className="px-3 py-1 bg-yellow-500 text-white rounded-md hover:bg-yellow-600 text-sm"
                                        disabled={archivo.estado !== "PROCESADO"}
                                    >
                                        <FaEdit className="inline" /> Renombrar
                                    </button>

                                    <button
                                        onClick={() => handleDescargar(archivo.id)}
                                        className={`px-3 py-1 bg-green-500 text-white rounded-md hover:bg-green-600 text-sm ${
                                            archivo.estado !== "PROCESADO"
                                                ? "opacity-50 cursor-not-allowed"
                                                : ""
                                        }`}
                                        disabled={archivo.estado !== "PROCESADO"}
                                    >
                                        <FaDownload className="inline" /> Descargar
                                    </button>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>

                <ToastContainer autoClose={3000} hideProgressBar closeOnClick pauseOnHover />
            </div>
        </div>
    );
};

export default Dashboard;
