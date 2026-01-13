<template>
  <q-page class="flex flex-center bg-grey-2">
    <q-card class="login-card q-pa-lg">
      <div class="text-center q-mb-md">
        <q-avatar size="80px" color="primary" text-color="white">
          <q-icon name="spa" size="40px" />
        </q-avatar>
        <div class="text-h6 q-mt-sm">Spa al Instante</div>
        <div class="text-caption text-grey">Iniciar sesión</div>
      </div>

      <q-input
        outlined
        dense
        v-model="correo"
        label="Correo"
        prepend-icon="email"
        @keyup.enter="login"
      />

      <q-input
        outlined
        dense
        v-model="password"
        label="Contraseña"
        type="password"
        prepend-icon="lock"
        class="q-mt-sm"
        @keyup.enter="login"
      />

      <q-btn
        label="Entrar"
        color="primary"
        class="full-width q-mt-lg"
        :loading="loading"
        icon="login"
        @click="login"
      />

      <q-btn flat label="Crear cuenta" to="/admin/register" class="full-width q-mt-sm" />
    </q-card>
  </q-page>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'
import { useAuthStore } from 'src/stores/auth'
import { useRouter } from 'vue-router'
import { useQuasar } from 'quasar'

const correo = ref('')
const password = ref('')
const loading = ref(false)

const auth = useAuthStore()
const router = useRouter()
const $q = useQuasar()

async function login() {
  if (!correo.value || !password.value) {
    $q.notify({
      type: 'warning',
      icon: 'warning',
      message: 'Ingresa correo y contraseña',
      position: 'top',
    })
    return
  }

  loading.value = true

  $q.loading.show({
    message: 'Iniciando sesión, por favor espera...',
  })

  try {
    const res = await axios.post('http://localhost:8082/api/auth/login', {
      correo: correo.value,
      password: password.value,
    })

    auth.login(res.data.token)

    $q.loading.hide()

    $q.notify({
      type: 'positive',
      icon: 'check_circle',
      message: 'Sesión iniciada correctamente',
      position: 'top',
      timeout: 2500,
    })

    router.push('/')
  } catch (error) {
    $q.loading.hide()

    if (error.response?.status === 404) {
      $q.notify({
        type: 'negative',
        icon: 'person_off',
        message: 'Usuario no registrado',
        position: 'top',
      })
    } else if (error.response?.status === 401) {
      $q.notify({
        type: 'negative',
        icon: 'lock',
        message: 'Contraseña incorrecta',
        position: 'top',
      })
    } else {
      $q.notify({
        type: 'negative',
        icon: 'error',
        message: 'Error al iniciar sesión',
        position: 'top',
      })
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-card {
  width: 100%;
  max-width: 360px;
  border-radius: 16px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}
</style>
