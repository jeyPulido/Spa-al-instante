<template>
  <q-page class="bg-secondary">
    <!-- HERO -->
    <div class="hero-section flex flex-center column text-white">
      <p class="text-h3 text-weight-bold q-mb-sm">Spa al Instante</p>
      <p class="text-subtitle1 q-mb-lg">Relájate, renueva tu energía y déjate consentir.</p>

      <q-btn label="Reserva ahora" color="primary" size="lg" href="#reservar" />
    </div>

    <!-- SERVICIOS -->
    <div id="servicios" class="section">
      <p class="section-title">Nuestros Servicios</p>

      <div class="servicios-grid">
        <q-card v-for="s in servicios" :key="s.id" class="my-card">
          <q-card-section class="text-center">
            <q-icon name="spa" size="50px" color="primary" />
            <p class="text-h6 q-mt-md">{{ s.nombre }}</p>
            <p class="text-body2">{{ s.descripcion }}</p>
            <p class="text-subtitle2 text-weight-bold q-mt-sm">$ {{ s.precio }} MXN</p>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <!-- RESERVAR CITA -->
    <div id="reservar" class="section">
      <p class="section-title">Reservar una Cita</p>

      <q-form @submit.prevent="crearCita" class="contact-form">
        <!-- SERVICIO -->
        <q-select
          filled
          v-model="cita.idServicio"
          :options="opcionesServicios"
          label="Selecciona un servicio"
          emit-value
          map-options
          required
        />

        <q-input filled v-model="cita.nombreCliente" label="Nombre completo" required />
        <q-input filled v-model="cita.email" type="email" label="Correo electrónico" required />
        <q-input filled v-model="cita.telefono" label="Teléfono" mask="##########" required />

        <!-- FECHA -->
        <q-input
          filled
          v-model="cita.fecha"
          type="date"
          label="Fecha"
          @update:model-value="validarYcargar"
          required
        />

        <!-- HORA -->
        <q-select
          filled
          v-model="cita.hora"
          :options="horarios"
          emit-value
          map-options
          label="Hora disponible"
          :disable="bloqueado"
          required
        />

        <q-btn
          label="Confirmar cita"
          type="submit"
          color="primary"
          size="lg"
          class="full-width q-mt-md"
        />
      </q-form>
    </div>

    <q-footer class="bg-dark text-white text-center q-pa-md"> © 2025 Spa al Instante </q-footer>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'

const $q = useQuasar()

// -------------------------
// SERVICIOS
// -------------------------
const servicios = ref([])
const opcionesServicios = ref([])

async function cargarServicios() {
  const res = await axios.get('http://localhost:8082/api/servicios')
  servicios.value = res.data
  opcionesServicios.value = res.data.map((s) => ({
    label: `${s.nombre} — $${s.precio}`,
    value: s.id,
  }))
}

// -------------------------
// DÍAS FESTIVOS MÉXICO 2025
// -------------------------
const diasFestivos = [
  '2025-01-01',
  '2025-02-05',
  '2025-03-21',
  '2025-05-01',
  '2025-09-16',
  '2025-11-20',
  '2025-12-25',
]

// -------------------------
// HORARIOS
// -------------------------
const horarios = ref([])
const bloqueado = ref(false)

async function validarYcargar() {
  const fecha = cita.value.fecha
  if (!fecha) return

  // Fecha local correcta
  const d = new Date(`${fecha}T00:00:00`)
  const hoy = new Date()
  const hoyClean = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate())

  // 1. Fecha pasada
  if (d < hoyClean) {
    bloquear('No puedes seleccionar una fecha pasada.')
    return
  }

  // 2. Domingo
  if (d.getDay() === 0) {
    bloquear('No trabajamos los domingos.')
    return
  }

  // 3. Día festivo
  if (diasFestivos.includes(fecha)) {
    bloquear('Esta fecha es día festivo y no se trabaja.')
    return
  }

  bloqueado.value = false
  await cargarHorarios()
}

function bloquear(msg) {
  bloqueado.value = true
  horarios.value = []
  cita.value.hora = ''
  $q.notify({ type: 'warning', message: msg })
}

async function cargarHorarios() {
  const res = await axios.get(
    `http://localhost:8082/api/horarios-disponibles?fecha=${cita.value.fecha}`,
  )

  horarios.value = res.data.map((h) => ({
    label: h,
    value: h,
  }))
}

// -------------------------
// CREAR CITA
// -------------------------
const cita = ref({
  idServicio: null,
  nombreCliente: '',
  email: '',
  telefono: '',
  fecha: '',
  hora: '',
})

async function crearCita() {
  try {
    const fechaHora = `${cita.value.fecha}T${cita.value.hora}:00`

    const body = {
      nombreCliente: cita.value.nombreCliente,
      correo: cita.value.email,
      telefono: cita.value.telefono,
      fechaHora,
      servicio: { id: cita.value.idServicio },
    }

    await axios.post('http://localhost:8082/api/citas', body)

    $q.notify({ type: 'positive', message: 'Cita creada y confirmación enviada.' })

    cita.value = {
      idServicio: null,
      nombreCliente: '',
      email: '',
      telefono: '',
      fecha: '',
      hora: '',
    }
  } catch (e) {
    console.error(e)
    $q.notify({
      type: 'negative',
      message: 'Error al registrar la cita.',
    })
  }
}

onMounted(() => {
  cargarServicios()
})
</script>

<style scoped>
.hero-section {
  height: 70vh;
  background: url('/statics/images.jpg') center/cover no-repeat;
}
.section {
  padding: 40px 20px;
}
.section-title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 25px;
}
.servicios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.contact-form {
  max-width: 600px;
  margin: auto;
}
.my-card {
  border-radius: 12px;
}
</style>
