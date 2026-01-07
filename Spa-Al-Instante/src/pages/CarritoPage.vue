<template>
  <q-page class="carrito-page q-pa-md">
    <!-- TÍTULO -->
    <div class="titulo-container">
      <p class="titulo-carrito">🛒 Tu Carrito</p>
      <div class="linea"></div>
    </div>

    <!-- SERVICIOS -->
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
        <q-input filled v-model="form.nombre" label="Nombre" :readonly="logueado" />
        <q-input filled v-model="form.apellidos" label="Apellidos" :readonly="logueado" />
        <q-input
          filled
          v-model="form.email"
          label="Correo electrónico"
          type="email"
          :readonly="logueado"
        />
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
        <q-card-section>
          <p class="text-weight-bold">🧾 Resumen</p>

          <p v-for="s in carrito" :key="s.id">• {{ s.nombre }} — ${{ s.precio }} MXN</p>

          <q-separator class="q-my-sm" />

          <p class="text-weight-bold text-right">Total: ${{ totalCarrito }} MXN</p>
          <p class="q-mt-sm">📅 {{ form.fecha || '—' }} — ⏰ {{ form.hora || '—' }}</p>
        </q-card-section>

        <q-btn
          label="Confirmar cita"
          class="btn-confirmar q-mt-lg"
          unelevated
          rounded
          @click="confirmar"
        />
      </q-form>
    </div>

    <!-- MODAL CONFIRMACIÓN -->
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
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'
import { useQuasar } from 'quasar'
import { useRouter } from 'vue-router'
import { useAuthStore } from 'src/stores/auth'

const $q = useQuasar()
const router = useRouter()
const auth = useAuthStore()

/* ================= STATE ================= */
const carrito = ref(JSON.parse(localStorage.getItem('carrito')) || [])

const form = ref({
  nombre: '',
  apellidos: '',
  email: '',
  telefono: '',
  fecha: '',
  hora: '',
})

const horarios = ref([])
const cargandoHorarios = ref(false)
const mostrarConfirmacion = ref(false)
const diasSaturados = ref([])

/* ================= COMPUTED ================= */
const logueado = computed(() => !!auth.user)
const totalCarrito = computed(() => carrito.value.reduce((t, i) => t + Number(i.precio), 0))

/* ================= WATCH ================= */
watch(
  carrito,
  (nuevo) => {
    if (nuevo.length === 0) {
      localStorage.removeItem('carrito')
      $q.notify({ type: 'info', message: 'Tu carrito está vacío' })
      router.replace('/')
    }
  },
  { deep: true },
)

/* ================= INIT ================= */
onMounted(async () => {
  auth.init()

  if (!carrito.value.length) {
    router.replace('/')
    return
  }

  // ✅ AUTOCOMPLETADO SI ESTÁ LOGUEADO
  if (auth.user) {
    form.value.nombre = auth.user.nombre
    form.value.apellidos = auth.user.apellidos
    form.value.email = auth.user.correo
    form.value.telefono = auth.user.telefono
  }

  const res = await axios.get('http://localhost:8082/api/dias-saturados')
  diasSaturados.value = res.data
})

/* ================= FECHAS ================= */
const hoy = new Date()
hoy.setHours(0, 0, 0, 0)
const limite = new Date(hoy)
limite.setMonth(limite.getMonth() + 2)

function opcionesCalendario(fecha) {
  const fechaISO = fecha.replace(/\//g, '-')
  const f = new Date(fechaISO + 'T00:00')
  if (f < hoy || f > limite || f.getDay() === 0) return false
  return !diasSaturados.value.includes(fechaISO)
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

  await axios.post('http://localhost:8082/api/citas', {
    nombreCliente: form.value.nombre,
    apellidosCliente: form.value.apellidos,
    correo: form.value.email,
    telefono: form.value.telefono,
    fechaHora,
    usuarioId: auth.user?.id || null,

    // ✅ LISTA DE SERVICIOS
    serviciosIds: carrito.value.map((s) => s.id),
  })

  $q.notify({
    type: 'positive',
    message: 'Cita registrada correctamente',
  })

  localStorage.removeItem('carrito')
  router.replace('/')
}

/* ================= ELIMINAR ================= */
function eliminar(i) {
  carrito.value.splice(i, 1)
  localStorage.setItem('carrito', JSON.stringify(carrito.value))
}
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
</style>
