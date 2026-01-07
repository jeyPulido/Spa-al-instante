<template>
  <q-page class="q-pa-md">
    <q-card class="q-pa-lg" style="max-width: 500px; margin: auto">
      <p class="text-h6 text-center">👤 Mi Perfil</p>

      <q-input filled v-model="form.nombre" label="Nombre" />
      <q-input filled v-model="form.apellidos" label="Apellidos" />
      <q-input filled v-model="form.correo" label="Correo" readonly />
      <q-input filled v-model="form.telefono" label="Teléfono" mask="##########" />

      <q-btn label="Guardar cambios" color="primary" class="q-mt-md full-width" @click="guardar" />
    </q-card>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from 'src/stores/auth'
import { useQuasar } from 'quasar'

const auth = useAuthStore()
const $q = useQuasar()

const form = ref({
  nombre: '',
  apellidos: '',
  correo: '',
  telefono: '',
})

onMounted(() => {
  auth.init()
  form.value = {
    nombre: auth.user.nombre,
    apellidos: auth.user.apellidos,
    correo: auth.user.correo,
    telefono: auth.user.telefono,
  }
})

async function guardar() {
  const res = await axios.put(`http://localhost:8082/api/usuario/perfil/${auth.user.id}`, {
    nombre: form.value.nombre,
    apellidos: form.value.apellidos,
    telefono: form.value.telefono,
  })

  // 🔥 reemplazar token
  auth.login(res.data)

  $q.notify({ type: 'positive', message: 'Perfil actualizado' })
}
</script>
