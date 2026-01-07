<template>
  <q-page class="flex flex-center bg-grey-2">
    <q-card class="q-pa-xl shadow-6" style="width: 420px; border-radius: 16px">
      <!-- HEADER -->
      <q-card-section class="text-center">
        <q-icon name="person_add" size="48px" color="primary" />
        <p class="text-h6 text-weight-bold q-mt-sm">Crear cuenta</p>
        <p class="text-caption text-grey-7">Completa los datos para registrar un usuario</p>
      </q-card-section>

      <!-- FORM -->
      <q-form @submit.prevent="registrar" class="q-gutter-md">
        <q-input
          v-model="form.nombre"
          label="Nombre"
          filled
          lazy-rules
          :rules="[(v) => !!v || 'Requerido']"
          prepend-icon="badge"
        />

        <q-input
          v-model="form.apellidos"
          label="Apellidos"
          filled
          lazy-rules
          :rules="[(v) => !!v || 'Requerido']"
          prepend-icon="badge"
        />

        <q-input
          v-model="form.correo"
          label="Correo electrónico"
          type="email"
          filled
          lazy-rules
          :rules="[(v) => !!v || 'Requerido', (v) => /.+@.+\..+/.test(v) || 'Correo inválido']"
          prepend-icon="email"
        />

        <q-input
          v-model="form.telefono"
          label="Teléfono"
          filled
          mask="##########"
          prepend-icon="phone"
        />

        <q-input
          v-model="form.password"
          label="Contraseña"
          type="password"
          filled
          lazy-rules
          :rules="[(v) => v.length >= 6 || 'Mínimo 6 caracteres']"
          prepend-icon="lock"
        />

        <!-- 👑 SOLO ADMIN -->
        <q-select
          v-if="esAdmin"
          v-model="form.rol"
          label="Rol del usuario"
          filled
          :options="roles"
          emit-value
          map-options
          prepend-icon="security"
        />

        <!-- BOTÓN -->
        <q-btn
          label="Registrar usuario"
          type="submit"
          color="primary"
          class="full-width q-mt-md"
          icon="how_to_reg"
          :loading="loading"
        />

        <q-btn flat label="Volver al login" color="grey-7" class="full-width" to="/admin/login" />
      </q-form>
    </q-card>
  </q-page>
</template>
<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuthStore } from 'src/stores/auth'
import { Notify } from 'quasar'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)

/* ================== FORM ================== */
const form = ref({
  nombre: '',
  apellidos: '',
  correo: '',
  telefono: '',
  password: '',
  rol: 'CLIENTE', // 👈 default
})

/* ================== ROLES ================== */
const esAdmin = computed(() => auth.user?.rol === 'ADMIN')

const roles = [
  { label: 'Usuario', value: 'CLIENTE' },
  { label: 'Administrador', value: 'ADMIN' },
]

/* ================== REGISTRO ================== */
async function registrar() {
  try {
    loading.value = true

    // 🔒 Si no es admin, forzar CLIENTE
    if (!esAdmin.value) {
      form.value.rol = 'CLIENTE'
    }

    await axios.post('http://localhost:8082/api/auth/register', form.value)

    Notify.create({
      type: 'positive',
      message: 'Usuario registrado correctamente',
      icon: 'check_circle',
    })

    router.push('/admin/login')
  } catch {
    Notify.create({
      type: 'negative',
      message: 'Error al registrar usuario',
      icon: 'error',
    })
  } finally {
    loading.value = false
  }
}
</script>
