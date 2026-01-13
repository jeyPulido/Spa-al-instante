<template>
  <q-page class="flex flex-center bg-grey-2">
    <q-slide-transition>
      <q-card
        ref="cardRef"
        class="q-pa-xl shadow-6 animated-card"
        style="width: 420px; border-radius: 16px"
      >
        <q-card-section class="text-center">
          <q-icon name="person_add" size="48px" color="primary" />
          <p class="text-h6 text-weight-bold q-mt-sm">Crear cuenta</p>
          <p class="text-caption text-grey-7">Completa los datos para registrar un usuario</p>
        </q-card-section>

        <q-form ref="formRef" @submit.prevent="registrar" class="q-gutter-md">
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
          <q-btn
            label="Registrar usuario"
            type="submit"
            color="primary"
            class="full-width q-mt-md"
            icon="how_to_reg"
            :loading="loading"
            v-ripple
          />

          <q-btn
            v-if="!esAdminLogueado"
            flat
            label="Volver al login"
            color="grey-7"
            class="full-width"
            to="/admin/login"
          />
        </q-form>
      </q-card>
    </q-slide-transition>
  </q-page>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuthStore } from 'src/stores/auth'
import { useQuasar } from 'quasar'

const $q = useQuasar()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const formRef = ref(null)
const cardRef = ref(null)

const form = ref({
  nombre: '',
  apellidos: '',
  correo: '',
  telefono: '',
  password: '',
  rol: 'CLIENTE',
})

const esAdmin = computed(() => auth.user?.rol === 'ADMIN')
const esAdminLogueado = computed(() => !!auth.token && auth.user?.rol === 'ADMIN')

const roles = [
  { label: 'Usuario', value: 'CLIENTE' },
  { label: 'Administrador', value: 'ADMIN' },
]

function animarExito() {
  cardRef.value?.$el.classList.add('pulse')
  setTimeout(() => {
    cardRef.value?.$el.classList.remove('pulse')
  }, 600)
}

function animarError() {
  cardRef.value?.$el.classList.add('shake')
  setTimeout(() => {
    cardRef.value?.$el.classList.remove('shake')
  }, 600)
}

function limpiarFormulario() {
  form.value = {
    nombre: '',
    apellidos: '',
    correo: '',
    telefono: '',
    password: '',
    rol: 'CLIENTE',
  }
  formRef.value?.resetValidation()
}

async function registrar() {
  loading.value = true

  $q.loading.show({
    message: 'Registrando usuario, por favor espera...',
  })

  try {
    if (!esAdmin.value) {
      form.value.rol = 'CLIENTE'
    }

    await axios.post('http://localhost:8082/api/auth/register', form.value)

    animarExito()

    $q.notify({
      type: 'positive',
      message: 'Usuario registrado correctamente',
      icon: 'check_circle',
      position: 'top',
      timeout: 3000,
    })

    if (!esAdminLogueado.value) {
      router.push('/admin/login')
    } else {
      limpiarFormulario()
    }
  } catch (error) {
    animarError()

    $q.notify({
      type: 'negative',
      message: error?.response?.data?.message || 'No se pudo registrar el usuario',
      icon: 'error',
      position: 'top',
      timeout: 4000,
    })
  } finally {
    loading.value = false
    $q.loading.hide()
  }
}
</script>

<style scoped>
.animated-card {
  transition: all 0.3s ease;
}

.pulse {
  animation: pulse 0.6s ease;
}
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.03);
  }
  100% {
    transform: scale(1);
  }
}

.shake {
  animation: shake 0.6s;
}
@keyframes shake {
  0% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-6px);
  }
  50% {
    transform: translateX(6px);
  }
  75% {
    transform: translateX(-6px);
  }
  100% {
    transform: translateX(0);
  }
}
</style>
