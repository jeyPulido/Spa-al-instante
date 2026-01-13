<template>
  <q-page class="q-pa-md flex flex-center bg-grey-2">
    <q-card class="perfil-card q-pa-lg">
      <div class="text-center q-mb-lg">
        <q-avatar size="96px" color="primary" text-color="white">
          {{ iniciales }}
        </q-avatar>

        <div class="text-h6 q-mt-sm">{{ form.nombre }} {{ form.apellidos }}</div>

        <q-badge color="secondary" outline class="q-mt-xs"> Perfil del cliente </q-badge>
      </div>

      <q-separator class="q-mb-md" />

      <q-banner v-if="hayCambios" rounded dense class="bg-warning text-dark q-mb-md">
        <template #avatar>
          <q-icon name="info" />
        </template>
        Tienes cambios sin guardar
      </q-banner>

      <div class="text-subtitle2 text-grey-7 q-mb-sm">Datos personales</div>

      <q-input
        outlined
        dense
        v-model="form.nombre"
        label="Nombre"
        prepend-icon="person"
        :rules="[(v) => !!v || 'El nombre es obligatorio']"
      />

      <q-input
        outlined
        dense
        v-model="form.apellidos"
        label="Apellidos"
        prepend-icon="badge"
        class="q-mt-sm"
        :rules="[(v) => !!v || 'Los apellidos son obligatorios']"
      />

      <q-input
        outlined
        dense
        v-model="form.correo"
        label="Correo"
        prepend-icon="email"
        readonly
        class="q-mt-sm"
      >
        <template #hint> El correo no se puede modificar </template>
      </q-input>

      <q-input
        outlined
        dense
        v-model="form.telefono"
        label="Teléfono"
        mask="##########"
        prepend-icon="phone"
        class="q-mt-sm"
        :rules="[(v) => v.length === 10 || 'Debe tener 10 dígitos']"
      />

      <div class="row q-col-gutter-sm q-mt-lg">
        <q-btn
          label="Guardar cambios"
          color="primary"
          class="col"
          icon="save"
          :loading="loading"
          :disable="!hayCambios"
          @click="confirmarGuardado"
        />

        <q-btn
          label="Restablecer"
          flat
          color="grey-7"
          icon="refresh"
          class="col"
          :disable="!hayCambios"
          @click="restablecer"
        />
      </div>
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

const original = ref({})

const iniciales = computed(() =>
  ((form.value.nombre?.[0] || '') + (form.value.apellidos?.[0] || '')).toUpperCase(),
)

const hayCambios = computed(() => JSON.stringify(form.value) !== JSON.stringify(original.value))

onMounted(() => {
  auth.init()

  $q.loading.show({ message: 'Cargando tu perfil...' })

  try {
    form.value = {
      nombre: auth.user.nombre,
      apellidos: auth.user.apellidos,
      correo: auth.user.correo,
      telefono: auth.user.telefono,
    }
    original.value = { ...form.value }
  } catch {
    $q.notify({
      type: 'negative',
      icon: 'error',
      message: 'No se pudo cargar la información del perfil',
    })
  } finally {
    $q.loading.hide()
  }
})

function restablecer() {
  form.value = { ...original.value }
}

function confirmarGuardado() {
  $q.dialog({
    title: 'Guardar cambios',
    message: '¿Deseas guardar los cambios en tu perfil?',
    cancel: true,
    persistent: true,
  }).onOk(guardar)
}

async function guardar() {
  loading.value = true
  $q.loading.show({ message: 'Guardando cambios en tu perfil...' })

  try {
    const res = await axios.put(`http://localhost:8082/api/usuario/perfil/${auth.user.id}`, {
      nombre: form.value.nombre,
      apellidos: form.value.apellidos,
      telefono: form.value.telefono,
    })

    auth.login(res.data)
    original.value = { ...form.value }

    $q.notify({
      type: 'positive',
      message: 'Perfil actualizado correctamente',
      icon: 'check_circle',
    })
  } catch {
    $q.notify({
      type: 'negative',
      message: 'No se pudo actualizar el perfil',
      icon: 'error',
    })
  } finally {
    loading.value = false
    $q.loading.hide()
  }
}
</script>

<style scoped>
.perfil-card {
  width: 100%;
  max-width: 420px;
  border-radius: 18px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
}
</style>
