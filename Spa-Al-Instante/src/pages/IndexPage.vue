<template>
  <q-page class="bg-secondary">
    <!-- 🛒 CARRITO FLOTANTE -->
    <q-page-sticky position="bottom-right" :offset="[18, 18]" class="carrito-flotante">
      <q-btn
        fab
        glossy
        color="primary"
        icon="shopping_cart"
        :disable="totalItems === 0"
        @click="irCarrito"
      >
        <q-badge v-if="totalItems > 0" color="red" floating :label="totalItems" />
      </q-btn>
    </q-page-sticky>

    <!-- 🌿 HERO -->
    <section class="hero-section">
      <div class="overlay"></div>
      <div class="hero-content text-center text-white">
        <img src="/statics/logo-spa.png" class="logo" alt="Logo Spa al Instante" />
        <p class="text-h3 text-weight-bold q-mt-md">Spa al Instante</p>
        <p class="text-subtitle1 q-mt-sm">Relájate, renueva tu energía y déjate consentir</p>
      </div>
    </section>

    <!-- 💆 SERVICIOS -->
    <section id="servicios" class="section">
      <p class="section-title">Nuestros Servicios</p>

      <div class="servicios-grid">
        <q-card v-for="s in servicios" :key="s.id" class="my-card">
          <q-card-section class="text-center">
            <q-icon name="spa" size="50px" color="primary" />
            <p class="text-h6 q-mt-md">{{ s.nombre }}</p>
            <p class="text-body2">{{ s.descripcion }}</p>
            <p class="text-subtitle2 text-weight-bold q-mt-sm">$ {{ s.precio }} MXN</p>

            <q-btn
              color="primary"
              label="Agregar al carrito"
              class="q-mt-md"
              @click="agregarCarrito(s)"
            />
          </q-card-section>
        </q-card>
      </div>
    </section>

    <!-- 🕒 HORARIOS -->
    <section class="section horarios-section">
      <p class="section-title">Horarios de Atención</p>

      <div class="horarios-card">
        <div class="horario-item">
          <q-icon name="event" size="28px" color="primary" />
          <div>
            <p class="horario-title">Días</p>
            <p>Lunes a Sábado</p>
          </div>
        </div>

        <div class="horario-item">
          <q-icon name="schedule" size="28px" color="primary" />
          <div>
            <p class="horario-title">Horario</p>
            <p>8:00 am – 8:00 pm</p>
          </div>
        </div>

        <div class="horario-item">
          <q-icon name="block" size="28px" color="negative" />
          <div>
            <p class="horario-title">No laboramos</p>
            <p>Días festivos oficiales</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 🖼️ GALERÍA -->
    <section class="section galeria-section">
      <p class="section-title">Nuestro Spa</p>

      <div class="galeria-grid">
        <img src="/statics/spa/spa1.jpg" loading="lazy" alt="Spa 1" />
        <img src="/statics/spa/spa2.jpg" loading="lazy" alt="Spa 2" />
        <img src="/statics/spa/spa3.jpg" loading="lazy" alt="Spa 3" />
        <img src="/statics/spa/spa4.jpg" loading="lazy" alt="Spa 4" />
        <img src="/statics/spa/spa5.jpg" loading="lazy" alt="Spa 5" />
        <img src="/statics/spa/spa6.jpg" loading="lazy" alt="Spa 6" />
      </div>
    </section>

    <!-- 📍 MAPA -->
    <section class="section mapa-section">
      <p class="section-title">Ubicación</p>

      <div class="mapa-container">
        <iframe
          src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d15029.205177928192!2d-99.22835534458014!3d19.657147199999997!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x85d21e363ffe0b93%3A0x1ff0c64b8c3f7c74!2sDepilaT!5e0!3m2!1ses-419!2smx!4v1767852264585!5m2!1ses-419!2smx"
          width="1200"
          height="450"
          style="border: 0"
          allowfullscreen=""
          loading="lazy"
          referrerpolicy="no-referrer-when-downgrade"
        ></iframe>
      </div>
    </section>

    <!-- 🧾 FOOTER -->
    <q-footer class="footer">
      <p>© 2025 Spa al Instante</p>
      <p>Bienestar • Relajación • Armonía</p>
    </q-footer>
  </q-page>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useQuasar } from 'quasar'
import { useRouter } from 'vue-router'
import { api } from 'boot/axios'

const $q = useQuasar()
const router = useRouter()

const servicios = ref([])
const carrito = ref([])

const totalItems = computed(() => carrito.value.reduce((sum, item) => sum + item.cantidad, 0))

async function cargarServicios() {
  try {
    const res = await api.get('/api/servicios')
    servicios.value = res.data
  } catch {
    $q.notify({
      type: 'negative',
      message: 'Error al cargar los servicios',
    })
  }
}

function cargarCarritoLocal() {
  const data = localStorage.getItem('carrito')
  carrito.value = data ? JSON.parse(data) : []
}

function guardarCarrito() {
  localStorage.setItem('carrito', JSON.stringify(carrito.value))
}

function agregarCarrito(servicio) {
  const existe = carrito.value.some((s) => s.id === servicio.id)

  if (existe) {
    $q.notify({
      type: 'warning',
      message: 'Este servicio ya está en el carrito',
    })
    return
  }

  carrito.value.push({ ...servicio, cantidad: 1 })
  guardarCarrito()

  $q.notify({
    type: 'positive',
    message: `${servicio.nombre} agregado al carrito`,
  })
}

function irCarrito() {
  if (totalItems.value === 0) {
    $q.notify({
      type: 'info',
      message: 'Tu carrito está vacío',
    })
    return
  }
  router.push('/carrito')
}

onMounted(() => {
  cargarServicios()
  cargarCarritoLocal()
})
</script>

<style scoped>
/* HERO */
.hero-section {
  height: 75vh;
  background: url('/statics/images.jpg') center/cover no-repeat;
  position: relative;
}

.overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
}

.hero-content {
  position: relative;
  z-index: 2;
  top: 50%;
  transform: translateY(-50%);
}

.logo {
  width: 120px;
}

/* SECCIONES */
.section {
  padding: 40px 20px;
}

.section-title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 25px;
}

/* SERVICIOS */
.servicios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.my-card {
  border-radius: 16px;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.my-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.12);
}

/* HORARIOS */
.horarios-section {
  background: #f8f6f3;
}

.horarios-card {
  max-width: 900px;
  margin: auto;
  background: white;
  padding: 30px;
  border-radius: 18px;
  display: flex;
  justify-content: space-around;
  gap: 20px;
  flex-wrap: wrap;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.horario-item {
  display: flex;
  gap: 15px;
  align-items: center;
}

.horario-title {
  font-weight: bold;
}

/* GALERÍA */
.galeria-grid {
  max-width: 1100px;
  margin: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.galeria-grid img {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 16px;
  transition: transform 0.3s ease;
}

.galeria-grid img:hover {
  transform: scale(1.05);
}

/* MAPA */
.mapa-container {
  max-width: 1100px;
  margin: auto;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

/* FOOTER */
.footer {
  background: #2f2f2f;
  color: white;
  text-align: center;
  padding: 20px;
}

/* CARRITO */
.carrito-flotante {
  z-index: 9999;
}
</style>
