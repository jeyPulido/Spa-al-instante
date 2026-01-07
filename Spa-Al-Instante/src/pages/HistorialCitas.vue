<template>
  <q-page class="q-pa-md bg-grey-2">
    <p class="text-h5 text-center q-mb-md">📅 Mis Citas</p>

    <q-card v-for="c in citas" :key="c.id" class="q-mb-md" bordered>
      <q-card-section>
        <!-- ESTADO -->
        <q-chip :color="colorEstado(c.estado)" text-color="white" class="q-mb-sm">
          {{ c.estado }}
        </q-chip>

        <!-- SERVICIOS -->
        <p class="text-weight-bold">💆 Servicios:</p>
        <ul>
          <li v-for="s in c.servicios" :key="s.id">
            {{ s.nombre }}
          </li>
        </ul>

        <p class="q-mt-sm">📅 {{ formatearFecha(c.fechaHora) }}</p>
        <p>📞 {{ c.telefono }}</p>

        <!-- ACCIONES -->
        <div class="row q-gutter-sm q-mt-md">
          <q-btn
            v-if="puedeCancelar(c)"
            color="negative"
            label="Cancelar"
            icon="cancel"
            @click="confirmarCancelacion(c.id)"
          />

          <q-btn
            v-if="puedeReagendar(c)"
            color="primary"
            label="Reagendar"
            icon="event"
            @click="reagendar(c)"
          />
        </div>
      </q-card-section>
    </q-card>

    <p v-if="!citas.length" class="text-center text-grey">No tienes citas registradas</p>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'
import { useAuthStore } from 'src/stores/auth'

const $q = useQuasar()
const auth = useAuthStore()
const citas = ref([])

onMounted(async () => {
  auth.init()
  if (!auth.user?.id) return

  const res = await axios.get(`http://localhost:8082/api/citas/usuario/${auth.user.id}`)
  citas.value = res.data
})

function formatearFecha(fecha) {
  return new Date(fecha).toLocaleString('es-MX', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function colorEstado(estado) {
  return (
    {
      PENDIENTE: 'orange',
      CONFIRMADA: 'blue',
      ATENDIDA: 'green',
      CANCELADA: 'red',
      REAGENDADA: 'purple',
    }[estado] || 'grey'
  )
}

function puedeCancelar(c) {
  return ['PENDIENTE', 'CONFIRMADA'].includes(c.estado)
}

function puedeReagendar(c) {
  return ['PENDIENTE', 'CONFIRMADA'].includes(c.estado)
}

function confirmarCancelacion(id) {
  $q.dialog({
    title: 'Cancelar cita',
    message: '¿Deseas cancelar esta cita?',
    cancel: true,
    persistent: true,
  }).onOk(() => cancelarCita(id))
}

async function cancelarCita(id) {
  await axios.delete(`http://localhost:8082/api/citas/${id}`)
  $q.notify({ type: 'positive', message: 'Cita cancelada' })
  citas.value = citas.value.map((c) => (c.id === id ? { ...c, estado: 'CANCELADA' } : c))
}
</script>
