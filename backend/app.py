import os
import secrets
import sqlite3

from pathlib import Path
from datetime import timedelta

from flask import Flask, request, jsonify
from flask_jwt_extended import (
    JWTManager,
    create_access_token,
    jwt_required,
    get_jwt_identity
)
from werkzeug.security import (
    generate_password_hash,
    check_password_hash
)

from database import obtener_conexion, inicializar_bd


# Configuración principal de Flask
app = Flask(__name__)

archivo_clave = Path(__file__).resolve().parent / "jwt_secret.key"

if not archivo_clave.exists():
    archivo_clave.write_text(
        secrets.token_hex(32),
        encoding="utf-8"
    )

app.config["JWT_SECRET_KEY"] = os.environ.get(
    "NOTASAPP_JWT_SECRET",
    archivo_clave.read_text(encoding="utf-8").strip()
)

app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=8)

jwt = JWTManager(app)

# Inicialización de la base de datos
inicializar_bd()


# Verificar disponibilidad del servidor
@app.route("/", methods=["GET"])
def verificar_servicio():

    return jsonify({
        "mensaje": "Servicio de NotasApp disponible."
    }), 200


# --------------------------------------------------
# REGISTRO DE USUARIOS
# --------------------------------------------------

@app.route("/api/registro", methods=["POST"])
def registrar_cuenta():

    datos_recibidos = request.get_json(silent=True)

    if not isinstance(datos_recibidos, dict):
        return jsonify({
            "mensaje": "No fue posible procesar la información enviada."
        }), 400

    nombre = datos_recibidos.get("nombre")
    correo = datos_recibidos.get("email")
    contrasena = datos_recibidos.get("password")

    if not all(
        isinstance(valor, str)
        for valor in [nombre, correo, contrasena]
    ):
        return jsonify({
            "mensaje": "Completa los datos solicitados para continuar."
        }), 400

    nombre = nombre.strip()
    correo = correo.strip().lower()

    if not nombre or not correo or not contrasena:
        return jsonify({
            "mensaje": "Completa los datos solicitados para continuar."
        }), 400

    if len(contrasena) < 8:
        return jsonify({
            "mensaje": "La contraseña debe contener al menos 8 caracteres."
        }), 400

    contrasena_protegida = generate_password_hash(contrasena)

    try:
        with obtener_conexion() as conexion:

            resultado = conexion.execute("""
                INSERT INTO usuarios (nombre, email, password)
                VALUES (?, ?, ?)
            """, (nombre, correo, contrasena_protegida))

            nuevo_usuario_id = resultado.lastrowid

        return jsonify({
            "mensaje": "Tu cuenta se ha creado correctamente.",
            "usuario_id": nuevo_usuario_id
        }), 201

    except sqlite3.IntegrityError:
        return jsonify({
            "mensaje": "Ya existe una cuenta asociada a este correo electrónico."
        }), 409


# --------------------------------------------------
# INICIO DE SESIÓN
# --------------------------------------------------

@app.route("/api/login", methods=["POST"])
def autenticar_usuario():

    datos_recibidos = request.get_json(silent=True)

    if not isinstance(datos_recibidos, dict):
        return jsonify({
            "mensaje": "No fue posible procesar la información enviada."
        }), 400

    correo = datos_recibidos.get("email")
    contrasena = datos_recibidos.get("password")

    if not isinstance(correo, str) or not isinstance(contrasena, str):
        return jsonify({
            "mensaje": "Completa tu correo electrónico y contraseña para continuar."
        }), 400

    correo = correo.strip().lower()

    if not correo or not contrasena:
        return jsonify({
            "mensaje": "Completa tu correo electrónico y contraseña para continuar."
        }), 400

    with obtener_conexion() as conexion:

        cuenta = conexion.execute("""
            SELECT id, nombre, email, password
            FROM usuarios
            WHERE email = ?
        """, (correo,)).fetchone()

    if cuenta is None or not check_password_hash(
        cuenta["password"], contrasena
    ):
        return jsonify({
            "mensaje": "No pudimos verificar tus datos. Revisa la información e inténtalo nuevamente."
        }), 401

    token_acceso = create_access_token(
        identity=str(cuenta["id"])
    )

    return jsonify({
        "mensaje": "¡Bienvenido a NotasApp!",
        "token": token_acceso,
        "usuario": {
            "id": cuenta["id"],
            "nombre": cuenta["nombre"],
            "email": cuenta["email"]
        }
    }), 200


# --------------------------------------------------
# PERFIL DEL USUARIO
# --------------------------------------------------

@app.route("/api/perfil", methods=["GET"])
@jwt_required()
def consultar_perfil():

    id_usuario = get_jwt_identity()

    with obtener_conexion() as conexion:

        cuenta = conexion.execute("""
            SELECT id, nombre, email
            FROM usuarios
            WHERE id = ?
        """, (id_usuario,)).fetchone()

    if cuenta is None:
        return jsonify({
            "mensaje": "No se encontró información de la cuenta solicitada."
        }), 404

    return jsonify({
        "id": cuenta["id"],
        "nombre": cuenta["nombre"],
        "email": cuenta["email"]
    }), 200


# --------------------------------------------------
# CREACIÓN DE NOTAS
# --------------------------------------------------

@app.route("/api/notas", methods=["POST"])
@jwt_required()
def crear_nota():

    id_usuario = get_jwt_identity()
    datos_recibidos = request.get_json(silent=True)

    if not isinstance(datos_recibidos, dict):
        return jsonify({
            "mensaje": "No fue posible procesar los datos de la nota."
        }), 400

    titulo = datos_recibidos.get("titulo")
    contenido = datos_recibidos.get("contenido")

    if not isinstance(titulo, str) or not isinstance(contenido, str):
        return jsonify({
            "mensaje": "Es necesario completar el título y contenido."
        }), 400

    titulo = titulo.strip()
    contenido = contenido.strip()

    if not titulo or not contenido:
        return jsonify({
            "mensaje": "Es necesario completar el título y contenido."
        }), 400

    if len(titulo) > 150:
        return jsonify({
            "mensaje": "El título no puede superar los 150 caracteres."
        }), 400

    with obtener_conexion() as conexion:

        resultado = conexion.execute("""
            INSERT INTO notas (usuario_id, titulo, contenido)
            VALUES (?, ?, ?)
        """, (id_usuario, titulo, contenido))

        nueva_nota_id = resultado.lastrowid

        nota_guardada = conexion.execute("""
            SELECT id, titulo, contenido, fecha_creacion
            FROM notas
            WHERE id = ?
        """, (nueva_nota_id,)).fetchone()

    return jsonify({
        "mensaje": "Tu nota se ha guardado exitosamente.",
        "nota": dict(nota_guardada)
    }), 201


# --------------------------------------------------
# CONSULTA DE NOTAS
# --------------------------------------------------

@app.route("/api/notas", methods=["GET"])
@jwt_required()
def consultar_notas():

    id_usuario = get_jwt_identity()

    with obtener_conexion() as conexion:

        registros = conexion.execute("""
            SELECT id, titulo, contenido, fecha_creacion
            FROM notas
            WHERE usuario_id = ?
            ORDER BY fecha_creacion DESC, id DESC
        """, (id_usuario,)).fetchall()

    listado_notas = [
        dict(registro) for registro in registros
    ]

    return jsonify({
        "mensaje": "Tus notas se han consultado correctamente.",
        "total": len(listado_notas),
        "notas": listado_notas
    }), 200


# --------------------------------------------------
# EJECUCIÓN DEL SERVIDOR
# --------------------------------------------------

if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5000,
        debug=False
    )