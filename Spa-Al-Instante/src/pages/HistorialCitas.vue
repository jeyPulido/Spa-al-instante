<template>
  <q-page class="q-pa-md bg-grey-2">
    <p class="text-h5 text-center q-mb-md">📅 Mis Citas</p>

    <q-card v-for="c in citasOrdenadas" :key="c.id" class="q-mb-md" bordered>
      <q-card-section>
        <q-chip :color="colorEstado(c.estado)" text-color="white" class="q-mb-sm">
          {{ c.estado }}
        </q-chip>

        <p class="text-weight-bold">💆 Servicios:</p>
        <ul>
          <li v-for="s in c.servicios" :key="s.id">
            {{ s.nombre }}
          </li>
        </ul>

        <p class="q-mt-sm">📅 {{ formatearFecha(c.fechaHora) }}</p>
        <p>📞 {{ c.telefono }}</p>

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
            @click="abrirReagendar(c)"
          />
        </div>
      </q-card-section>
    </q-card>

    <p v-if="!citasOrdenadas.length" class="text-center text-grey">No tienes citas registradas</p>

    <!-- 🔁 DIALOG REAGENDAR -->
    <q-dialog v-model="dialogReagendar">
      <q-card style="min-width: 350px">
        <q-card-section class="text-h6"> Reagendar cita </q-card-section>

        <q-card-section>
          <q-date
            v-model="fechaSeleccionada"
            mask="YYYY-MM-DD"
            minimal
            :options="opcionesCalendario"
          />

          <q-select
            class="q-mt-md"
            v-model="horaSeleccionada"
            label="Horario disponible"
            :options="horariosDisponibles"
            option-label="hora"
            option-value="hora"
            emit-value
            map-options
            :disable="!horariosDisponibles.length"
          />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn
            color="primary"
            label="Confirmar"
            :disable="!horaSeleccionada"
            @click="confirmarReagendar"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'
import { useAuthStore } from 'src/stores/auth'

const $q = useQuasar()
const auth = useAuthStore()

const citas = ref([])
const diasSaturados = ref([])

/* 🔢 PRIORIDAD */
const prioridadEstados = {
  CONFIRMADA: 1,
  PENDIENTE: 2,
  REAGENDADA: 3,
  ATENDIDA: 4,
  CANCELADA: 5,
}

const citasOrdenadas = computed(() =>
  [...citas.value].sort(
    (a, b) => (prioridadEstados[a.estado] || 99) - (prioridadEstados[b.estado] || 99),
  ),
)

/* 🔁 REAGENDAR STATE */
const dialogReagendar = ref(false)
const citaSeleccionada = ref(null)
const fechaSeleccionada = ref(null)
const horaSeleccionada = ref(null)
const horariosDisponibles = ref([])

/* 📅 FECHAS (IGUAL QUE AGENDAR) */
const hoy = new Date()
hoy.setHours(0, 0, 0, 0)

const limite = new Date(hoy)
limite.setMonth(limite.getMonth() + 2)

function opcionesCalendario(fecha) {
  const fechaISO = fecha.replace(/\//g, '-')
  const f = new Date(fechaISO + 'T00:00')

  if (f < hoy || f > limite) return false
  if (f.getDay() === 0) return false // domingo
  if (diasSaturados.value.includes(fechaISO)) return false

  return true
}

/* 🚀 INIT */
onMounted(async () => {
  auth.init()
  if (!auth.user?.id) return

  const [citasRes, diasRes] = await Promise.all([
    axios.get(`http://localhost:8082/api/citas/usuario/${auth.user.id}`),
    axios.get('http://localhost:8082/api/dias-saturados'),
  ])

  citas.value = citasRes.data
  diasSaturados.value = diasRes.data
})

/* 🧾 UTILS */
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

/* ❌ CANCELAR */
function confirmarCancelacion(id) {
  $q.dialog({
    title: 'Cancelar cita',
    message: '¿Deseas cancelar esta cita?',
    cancel: true,
    persistent: true,
  }).onOk(async () => {
    await axios.delete(`http://localhost:8082/api/citas/${id}`)
    citas.value = citas.value.map((c) => (c.id === id ? { ...c, estado: 'CANCELADA' } : c))
    $q.notify({ type: 'positive', message: 'Cita cancelada' })
  })
}

/* 🔁 REAGENDAR */
function abrirReagendar(cita) {
  citaSeleccionada.value = cita
  fechaSeleccionada.value = null
  horaSeleccionada.value = null
  horariosDisponibles.value = []
  dialogReagendar.value = true
}

/* ⏰ HORARIOS (IGUAL QUE AGENDAR) */
watch(fechaSeleccionada, async (fecha) => {
  if (!fecha) return

  const res = await axios.get('http://localhost:8082/api/horarios-disponibles', {
    params: { fecha },
  })

  const ahora = new Date()
  const esHoy = fecha === ahora.toISOString().slice(0, 10)
  const horaActualMin = ahora.getHours() * 60 + ahora.getMinutes()

  horariosDisponibles.value = res.data.filter((h) => {
    if (!esHoy) return true

    const [hh, mm] = h.hora.split(':').map(Number)
    const minutosHorario = hh * 60 + mm

    return minutosHorario > horaActualMin
  })
})

/* ✅ CONFIRMAR REAGENDAR */
async function confirmarReagendar() {
  const fechaHora = `${fechaSeleccionada.value}T${horaSeleccionada.value}`

  const res = await axios.patch(
    `http://localhost:8082/api/citas/${citaSeleccionada.value.id}/reagendar`,
    null,
    { params: { nuevaFechaHora: fechaHora } },
  )

  citas.value = citas.value.map((c) => (c.id === res.data.id ? res.data : c))

  dialogReagendar.value = false

  $q.notify({
    type: 'positive',
    message: 'Cita reagendada correctamente',
  })
}
</script>
