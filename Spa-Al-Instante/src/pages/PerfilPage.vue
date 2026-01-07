<template>
  <q-page class="q-pa-md flex flex-center">
    <q-card class="perfil-card q-pa-lg">
      <!-- AVATAR -->
      <div class="text-center q-mb-md">
        <q-avatar size="90px" color="primary" text-color="white">
          {{ iniciales }}
        </q-avatar>
        <div class="text-h6 q-mt-sm">{{ form.nombre }} {{ form.apellidos }}</div>
        <div class="text-caption text-grey">Información personal</div>
      </div>

      <q-separator class="q-my-md" />

      <!-- FORM -->
      <q-input outlined dense v-model="form.nombre" label="Nombre" prepend-icon="person" />

      <q-input
        outlined
        dense
        v-model="form.apellidos"
        label="Apellidos"
        prepend-icon="badge"
        class="q-mt-sm"
      />

      <q-input
        outlined
        dense
        v-model="form.correo"
        label="Correo"
        prepend-icon="email"
        readonly
        class="q-mt-sm"
      />

      <q-input
        outlined
        dense
        v-model="form.telefono"
        label="Teléfono"
        mask="##########"
        prepend-icon="phone"
        class="q-mt-sm"
      />

      <q-btn
        label="Guardar cambios"
        color="primary"
        class="q-mt-lg full-width"
        :loading="loading"
        icon="save"
        @click="guardar"
      />
    </q-card>
  </q-page>
</template>
<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useAuthStore } from 'src/stores/auth'
import { useQuasar } from 'quasar'

const auth = useAuthStore()
const $q = useQuasar()
const loading = ref(false)

const form = ref({
  nombre: '',
  apellidos: '',
  correo: '',
  telefono: '',
})

const iniciales = computed(() => {
  return ((form.value.nombre?.[0] || '') + (form.value.apellidos?.[0] || '')).toUpperCase()
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
  loading.value = true
  try {
    const res = await axios.put(`http://localhost:8082/api/usuario/perfil/${auth.user.id}`, {
      nombre: form.value.nombre,
      apellidos: form.value.apellidos,
      telefono: form.value.telefono,
    })

    auth.login(res.data)

    $q.notify({
      type: 'positive',
      message: 'Perfil actualizado correctamente',
    })
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.perfil-card {
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
}
</style>
