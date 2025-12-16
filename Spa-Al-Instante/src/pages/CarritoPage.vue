<template>
  <q-page class="carrito-page q-pa-md">
    <!-- TÍTULO -->
    <div class="titulo-container">
      <p class="titulo-carrito">🛒 Tu Carrito</p>
      <div class="linea"></div>
    </div>

    <!-- SERVICIOS (MISMO ANCHO QUE FORMULARIO) -->
    <div class="contenedor-central">
      <q-card
        v-for="(item, index) in carrito"
        :key="index"
        class="servicio-card q-mb-md"
        flat
        bordered
      >
        <q-card-section class="row items-center">
          <div class="col-9">
            <p class="text-weight-bold">{{ item.nombre }}</p>
            <p class="text-grey-7">$ {{ item.precio }} MXN</p>
          </div>

          <div class="col-3 text-right">
            <q-btn icon="delete" color="negative" flat round @click="eliminar(index)" />
          </div>
        </q-card-section>
      </q-card>
    </div>

    <!-- FORMULARIO -->
    <div v-if="carrito.length" class="formulario-container q-mt-lg">
      <p class="form-title">Datos para confirmar tu cita</p>

      <q-form>
        <q-input filled v-model="form.nombre" label="Nombre completo" />
        <q-input filled v-model="form.email" label="Correo electrónico" type="email" />
        <q-input filled v-model="form.telefono" label="Teléfono" mask="##########" />

        <!-- FECHA -->
        <q-input filled v-model="form.fecha" label="Fecha" readonly>
          <template #append>
            <q-icon name="event" class="cursor-pointer" />
          </template>

          <q-popup-proxy cover transition-show="scale" transition-hide="scale">
            <q-date
              v-model="form.fecha"
              mask="YYYY-MM-DD"
              :options="opcionesCalendario"
              @update:model-value="alCambiarFecha"
              minimal
            />
          </q-popup-proxy>
        </q-input>

        <!-- HORARIOS -->
        <q-select
          filled
          v-model="form.hora"
          label="Hora disponible"
          :options="horarios"
          emit-value
          map-options
          :loading="cargandoHorarios"
          :disable="!form.fecha || cargandoHorarios"
        />

        <!-- RESUMEN -->
        <q-card flat bordered class="q-mt-md resumen-card">
          <q-card-section>
            <p class="text-weight-bold">🧾 Resumen</p>
            <p v-for="s in carrito" :key="s.id">• {{ s.nombre }} — ${{ s.precio }}</p>
            <p class="q-mt-sm">📅 {{ form.fecha || '—' }} — ⏰ {{ form.hora || '—' }}</p>
          </q-card-section>
        </q-card>

        <q-btn
          label="Confirmar cita"
          class="btn-confirmar q-mt-lg"
          unelevated
          rounded
          @click="confirmar"
        />
      </q-form>
    </div>

    <!-- MODAL -->
    <q-dialog v-model="mostrarConfirmacion">
      <q-card class="q-pa-md">
        <q-card-section class="text-center">
          <p class="text-h6">¿Confirmar cita?</p>
          <p>{{ form.fecha }} a las {{ form.hora }}</p>
        </q-card-section>

        <q-card-actions align="between">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn label="Confirmar" color="primary" @click="enviarCita" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useQuasar } from 'quasar'
import { useRouter } from 'vue-router'

const $q = useQuasar()
const router = useRouter()

/* ================= ESTADO ================= */
const carrito = ref(JSON.parse(localStorage.getItem('carrito')) || [])

const form = ref({
  nombre: '',
  email: '',
  telefono: '',
  fecha: '',
  hora: '',
})

const horarios = ref([])
const cargandoHorarios = ref(false)
const mostrarConfirmacion = ref(false)

/* ================= FECHAS ================= */
const hoy = new Date()
hoy.setHours(0, 0, 0, 0)

const limite = new Date(hoy)
limite.setMonth(limite.getMonth() + 2)

function obtenerFestivosMX(year) {
  return [
    `${year}-01-01`, // Año Nuevo
    `${year}-02-05`, // Constitución
    `${year}-03-21`, // Natalicio de Benito Juárez
    `${year}-05-01`, // Día del trabajo
    `${year}-09-16`, // Independencia
    `${year}-11-20`, // Revolución
    `${year}-12-25`, // Navidad
  ]
}

const diasSaturados = ref([])

function opcionesCalendario(fecha) {
  const fechaISO = fecha.replace(/\//g, '-')
  const f = new Date(fechaISO + 'T00:00')
  const year = f.getFullYear()

  const festivos = obtenerFestivosMX(year)

  if (f < hoy) return false
  if (f > limite) return false
  if (f.getDay() === 0) return false
  if (festivos.includes(fechaISO)) return false
  if (diasSaturados.value.includes(fechaISO)) return false

  return true
}

/* ================= HORARIOS ================= */
async function alCambiarFecha() {
  horarios.value = []
  form.value.hora = ''
  cargandoHorarios.value = true

  try {
    const res = await axios.get(
      `http://localhost:8082/api/horarios-disponibles?fecha=${form.value.fecha}`,
    )

    horarios.value = res.data.map((h) => ({
      label: h.hora,
      value: h.hora,
      disable: !h.disponible,
    }))
  } finally {
    cargandoHorarios.value = false
  }
}

/* ================= CONFIRMAR ================= */
function confirmar() {
  if (!form.value.fecha || !form.value.hora) {
    $q.notify({ type: 'warning', message: 'Selecciona fecha y hora' })
    return
  }
  mostrarConfirmacion.value = true
}

async function enviarCita() {
  mostrarConfirmacion.value = false

  const fechaHora = `${form.value.fecha}T${form.value.hora}:00`

  for (const s of carrito.value) {
    await axios.post('http://localhost:8082/api/citas', {
      nombreCliente: form.value.nombre,
      correo: form.value.email,
      telefono: form.value.telefono,
      fechaHora,
      servicioId: s.id,
    })
  }

  $q.notify({ type: 'positive', message: 'Cita registrada correctamente' })
  localStorage.removeItem('carrito')
  router.push('/')
}

/* ================= OTROS ================= */
function eliminar(i) {
  carrito.value.splice(i, 1)
  localStorage.setItem('carrito', JSON.stringify(carrito.value))
}

onMounted(async () => {
  const res = await axios.get('http://localhost:8082/api/dias-saturados')
  diasSaturados.value = res.data
})
</script>

<style scoped>
.carrito-page {
  background: linear-gradient(to bottom, #f7f4f0, #efe8e1);
}

.contenedor-central {
  max-width: 600px;
  margin: auto;
}

.servicio-card {
  border-radius: 14px;
}

.formulario-container {
  max-width: 600px;
  margin: auto;
  background: white;
  padding: 24px;
  border-radius: 18px;
}

.resumen-card {
  background: #faf7f3;
}

.btn-confirmar {
  width: 100%;
  background: #9c7d60;
  color: white;
}

.titulo-carrito {
  text-align: center;
  font-size: 1.6rem;
  font-weight: bold;
}
.q-popup-proxy {
  background: transparent !important;
}

.q-date {
  box-shadow: none !important;
  border-radius: 10px;
}
</style>
