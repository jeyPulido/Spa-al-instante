<template>
  <q-page class="q-pa-md bg-grey-2">
    <div class="text-center q-mb-md">
      <div class="text-h5 text-weight-bold">📅 Mis Citas</div>
      <div class="text-caption text-grey">
        Consulta, cancela o reagenda tus citas en Spa al Instante
      </div>
    </div>

    <q-banner v-if="!loading && citas.length" rounded class="bg-primary text-white q-mb-md">
      <template #avatar>
        <q-icon name="spa" />
      </template>
      Tienes <strong>{{ citas.length }}</strong> cita(s) registradas. ¡Gracias por confiar en
      nosotros!
    </q-banner>

    <q-banner v-if="errorCarga" rounded class="bg-negative text-white q-mb-md">
      <template #avatar>
        <q-icon name="error" />
      </template>
      No fue posible cargar tus citas. Intenta más tarde.
    </q-banner>

    <div class="relative-position">
      <q-inner-loading
        :showing="loading"
        label="Cargando tus citas..."
        label-class="text-primary"
        color="primary"
        size="60px"
      />
    </div>

    <div v-if="!loading && !errorCarga">
      <q-card v-for="c in citasOrdenadas" :key="c.id" class="q-mb-md" bordered>
        <q-card-section>
          <q-chip :color="colorEstado(c.estado)" text-color="white" class="q-mb-sm">
            {{ labelEstado(c.estado) }}
          </q-chip>

          <div class="q-mt-sm">
            <div class="text-weight-bold">💆 Servicios</div>
            <q-chip
              v-for="s in c.servicios"
              :key="s.id"
              dense
              outline
              color="primary"
              class="q-mr-xs q-mt-xs"
            >
              {{ s.nombre }}
            </q-chip>
          </div>

          <div class="q-mt-sm">
            <q-icon name="event" class="q-mr-xs" />
            {{ formatearFecha(c.fechaHora) }}
          </div>

          <div class="text-grey-7">
            <q-icon name="phone" class="q-mr-xs" />
            {{ c.telefono }}
          </div>

          <div v-if="mostrarAcciones(c)" class="row q-gutter-sm q-mt-md">
            <q-btn
              color="negative"
              icon="cancel"
              label="Cancelar"
              :loading="procesando"
              @click="confirmarCancelacion(c)"
            />
            <q-btn color="primary" icon="event" label="Reagendar" @click="abrirReagendar(c)" />
          </div>
        </q-card-section>
      </q-card>

      <div v-if="!citas.length" class="text-center text-grey q-mt-xl">
        <q-icon name="event_busy" size="72px" />
        <div class="text-h6 q-mt-sm">Aún no tienes citas registradas</div>
        <div class="text-caption">Agenda tu primera cita y relájate 💆‍♀️</div>
      </div>
    </div>

    <q-dialog v-model="dialogReagendar" persistent>
      <q-card style="min-width: 360px">
        <q-card-section class="text-h6">🔄 Reagendar cita</q-card-section>

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
            emit-value
            map-options
            :loading="cargandoHorarios"
            :disable="!horariosDisponibles.length"
          />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn
            color="primary"
            label="Confirmar"
            :loading="procesando"
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

const loading = ref(true)
const errorCarga = ref(false)
const procesando = ref(false)

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

onMounted(async () => {
  auth.init()
  if (!auth.user?.id) return

  $q.loading.show({ message: 'Cargando tus citas...' })

  try {
    const [citasRes, diasRes] = await Promise.all([
      axios.get(`http://localhost:8082/api/citas/usuario/${auth.user.id}`),
      axios.get('http://localhost:8082/api/dias-saturados'),
    ])

    citas.value = citasRes.data
    diasSaturados.value = diasRes.data
  } catch {
    errorCarga.value = true
    $q.notify({ type: 'negative', message: 'No se pudieron cargar las citas' })
  } finally {
    loading.value = false
    $q.loading.hide()
  }
})

const hoy = new Date()
hoy.setHours(0, 0, 0, 0)

const limite = new Date(hoy)
limite.setMonth(limite.getMonth() + 2)

function opcionesCalendario(fecha) {
  const fechaISO = fecha.replace(/\//g, '-')
  const f = new Date(fechaISO + 'T00:00')

  if (f < hoy) return false
  if (f > limite) return false
  if (f.getDay() === 0) return false

  return !diasSaturados.value.includes(fechaISO)
}

function confirmarCancelacion(cita) {
  $q.dialog({
    title: 'Cancelar cita',
    message: '¿Deseas cancelar esta cita?',
    cancel: true,
    persistent: true,
  }).onOk(async () => {
    procesando.value = true
    $q.loading.show({ message: 'Cancelando cita...' })

    try {
      await axios.delete(`http://localhost:8082/api/citas/${cita.id}`)
      cita.estado = 'CANCELADA'
      $q.notify({ type: 'positive', message: 'Cita cancelada correctamente' })
    } catch {
      $q.notify({ type: 'negative', message: 'No se pudo cancelar la cita' })
    } finally {
      procesando.value = false
      $q.loading.hide()
    }
  })
}

const dialogReagendar = ref(false)
const citaSeleccionada = ref(null)
const fechaSeleccionada = ref(null)
const horaSeleccionada = ref(null)

const horariosDisponibles = ref([])
const cargandoHorarios = ref(false)

function abrirReagendar(cita) {
  citaSeleccionada.value = cita
  fechaSeleccionada.value = null
  horaSeleccionada.value = null
  horariosDisponibles.value = []
  dialogReagendar.value = true
}

watch(fechaSeleccionada, async (fecha) => {
  if (!fecha) return

  cargandoHorarios.value = true
  horariosDisponibles.value = []

  try {
    const res = await axios.get(`http://localhost:8082/api/horarios-disponibles?fecha=${fecha}`)

    const ahora = new Date()
    const hoyLocal = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(
      2,
      '0',
    )}-${String(ahora.getDate()).padStart(2, '0')}`

    const esHoy = fecha === hoyLocal
    const minutosActuales = ahora.getHours() * 60 + ahora.getMinutes()

    horariosDisponibles.value = res.data.map((h) => {
      const [hh, mm] = h.hora.split(':').map(Number)
      const minutosHora = hh * 60 + mm

      return {
        label: h.hora,
        value: h.hora,
        disable: !h.disponible || (esHoy && minutosHora <= minutosActuales),
      }
    })
  } catch {
    $q.notify({
      type: 'negative',
      message: 'No se pudieron cargar los horarios',
    })
  } finally {
    cargandoHorarios.value = false
  }
})

async function confirmarReagendar() {
  if (!fechaSeleccionada.value || !horaSeleccionada.value) return

  procesando.value = true
  $q.loading.show({ message: 'Reagendando cita...' })

  try {
    const fechaHora = `${fechaSeleccionada.value}T${horaSeleccionada.value}:00`

    const res = await axios.patch(
      `http://localhost:8082/api/citas/${citaSeleccionada.value.id}/reagendar`,
      null,
      { params: { nuevaFechaHora: fechaHora } },
    )

    citas.value = citas.value.map((c) => (c.id === res.data.id ? res.data : c))
    dialogReagendar.value = false

    $q.notify({ type: 'positive', message: 'Cita reagendada correctamente' })
  } catch {
    $q.notify({ type: 'negative', message: 'No se pudo reagendar la cita' })
  } finally {
    procesando.value = false
    $q.loading.hide()
  }
}

function formatearFecha(fechaHora) {
  return new Date(fechaHora).toLocaleString('es-MX', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function mostrarAcciones(c) {
  return ['PENDIENTE', 'CONFIRMADA'].includes(c.estado)
}

function colorEstado(e) {
  return {
    PENDIENTE: 'orange',
    CONFIRMADA: 'blue',
    ATENDIDA: 'green',
    CANCELADA: 'red',
  }[e]
}

function labelEstado(e) {
  return {
    PENDIENTE: 'Pendiente',
    CONFIRMADA: 'Confirmada',
    ATENDIDA: 'Atendida',
    CANCELADA: 'Cancelada',
  }[e]
}
</script>
