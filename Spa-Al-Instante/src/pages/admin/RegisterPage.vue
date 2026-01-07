<template>
  <q-page class="flex flex-center">
    <q-card class="q-pa-lg" style="width: 400px">
      <q-card-section>
        <p class="text-h6 text-center">Registro</p>
      </q-card-section>

      <q-input v-model="form.nombre" label="Nombre" filled />
      <q-input v-model="form.apellidos" label="Apellidos" filled />
      <q-input v-model="form.correo" label="Correo" filled />
      <q-input v-model="form.telefono" label="Teléfono" filled />
      <q-input v-model="form.password" label="Contraseña" type="password" filled />

      <q-btn label="Registrarme" color="primary" class="full-width q-mt-md" @click="registrar" />
    </q-card>
  </q-page>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({
  nombre: '',
  apellidos: '',
  correo: '',
  telefono: '',
  password: '',
})

async function registrar() {
  await axios.post('http://localhost:8082/api/auth/register', form.value)
  router.push('/login')
}
</script>
