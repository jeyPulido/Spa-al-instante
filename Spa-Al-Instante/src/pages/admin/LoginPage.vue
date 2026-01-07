<template>
  <q-page class="flex flex-center">
    <q-card class="q-pa-lg" style="width: 350px">
      <q-card-section>
        <p class="text-h6 text-center">Iniciar Sesión</p>
      </q-card-section>

      <q-input v-model="correo" label="Correo" filled />
      <q-input v-model="password" label="Contraseña" type="password" filled />

      <q-btn label="Entrar" color="primary" class="full-width q-mt-md" @click="login" />
      <q-btn flat label="Registrarme" to="/registro" class="full-width q-mt-sm" />
    </q-card>
  </q-page>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'
import { useAuthStore } from 'src/stores/auth'
import { useRouter } from 'vue-router'

const correo = ref('')
const password = ref('')
const auth = useAuthStore()
const router = useRouter()

async function login() {
  const res = await axios.post('http://localhost:8082/api/auth/login', {
    correo: correo.value,
    password: password.value,
  })

  auth.login(res.data.token)
  router.push(auth.user.rol === 'ADMIN' ? '/admin' : '/')
}
</script>
