<template>
  <q-page class="bg-secondary">
    <!-- 🛒 CARRITO FLOTANTE -->
    <q-page-sticky position="bottom-right" :offset="[18, 18]" class="carrito-flotante">
      <q-btn fab color="primary" icon="shopping_cart" @click="irCarrito">
        <q-badge v-if="carrito.length > 0" color="red" floating :label="carrito.length" />
      </q-btn>
    </q-page-sticky>

    <!-- 🌿 HERO -->
    <section class="hero-section">
      <div class="overlay"></div>
      <div class="hero-content text-center text-white">
        <img src="/statics/logo-spa.png" class="logo" />
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

    <!-- 🕒 HORARIOS PROFESIONALES -->
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
        <img src="/statics/spa/spa1.jpg" />
        <img src="/statics/spa/spa2.jpg" />
        <img src="/statics/spa/spa3.jpg" />
        <img src="/statics/spa/spa4.jpg" />
        <img src="/statics/spa/spa5.jpg" />
        <img src="/statics/spa/spa6.jpg" />
      </div>
    </section>

    <!-- 📍 MAPA -->
    <section class="section mapa-section">
      <p class="section-title">Ubicación</p>

      <div class="mapa-container">
        <iframe
          src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3757.5483432745373!2d-99.23297642660069!3d19.64659799334239!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x85d21e5c07387a6f%3A0xd7735472495ee4aa!2sTesci%20-%20Tecnol%C3%B3gico%20de%20Estudios%20Superiores%20de%20Cuautitl%C3%A1n%20Izcalli!5e0!3m2!1ses-419!2sus!4v1767628031141!5m2!1ses-419!2sus"
          width="100%"
          height="350"
          style="border: 0"
          allowfullscreen
          loading="lazy"
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
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'
import { useRouter } from 'vue-router'

const $q = useQuasar()
const router = useRouter()

const servicios = ref([])
const carrito = ref([])

async function cargarServicios() {
  const res = await axios.get('http://localhost:8082/api/servicios')
  servicios.value = res.data
}

function cargarCarritoLocal() {
  const data = localStorage.getItem('carrito')
  carrito.value = data ? JSON.parse(data) : []
}

function guardarCarrito() {
  localStorage.setItem('carrito', JSON.stringify(carrito.value))
}

function agregarCarrito(servicio) {
  carrito.value.push(servicio)
  guardarCarrito()
  $q.notify({
    type: 'positive',
    message: `${servicio.nombre} agregado al carrito`,
  })
}

function irCarrito() {
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

/* INFO */
.info-bar {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 30px 15px;
  flex-wrap: wrap;
}

.info-card {
  display: flex;
  gap: 15px;
  padding: 20px;
  border-radius: 14px;
  min-width: 260px;
}

/* SERVICIOS */
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
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.my-card {
  border-radius: 16px;
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

.carrito-flotante {
  z-index: 9999;
}
</style>
