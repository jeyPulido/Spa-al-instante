<template>
  <q-page class="bg-secondary">
    <q-page-sticky position="bottom-right" :offset="[18, 18]" class="carrito-flotante">
      <q-btn
        fab
        glossy
        color="primary"
        icon="shopping_cart"
        :disable="totalItems === 0"
        @click="confirmarIrCarrito"
      >
        <q-badge v-if="totalItems > 0" color="red" floating :label="totalItems" />
      </q-btn>
    </q-page-sticky>

    <section class="hero-section">
      <div class="overlay"></div>
      <div class="hero-content text-center text-white">
        <img src="/statics/logo-spa.png" class="logo" alt="Logo Spa al Instante" />
        <p class="text-h3 text-weight-bold q-mt-md">Spa al Instante</p>
        <p class="text-subtitle1 q-mt-sm">Relájate, renueva tu energía y déjate consentir</p>
      </div>
    </section>

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

    <section class="section mapa-section">
      <p class="section-title">Ubicación</p>

      <div class="mapa-container">
        <iframe
          src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d15029.205177928192!2d-99.22835534458014!3d19.657147199999997!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x85d21e363ffe0b93%3A0x1ff0c64b8c3f7c74!2sDepilaT!5e0!3m2!1ses-419!2smx!4v1767852264585!5m2!1ses-419!2smx"
          width="1200"
          height="450"
          style="border: 0"
          allowfullscreen
          loading="lazy"
        ></iframe>
      </div>
    </section>

    <q-footer class="footer">
      <p>© 2026 Spa al Instante</p>
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
  $q.loading.show({ message: 'Cargando servicios, por favor espera...' })

  try {
    const res = await api.get('/api/servicios')
    servicios.value = res.data
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'No fue posible cargar los servicios',
      caption: error?.response
        ? `Error ${error.response.status}: ${error.response.statusText}`
        : 'Error de conexión con el servidor',
      icon: 'error',
    })
  } finally {
    $q.loading.hide()
  }
}

function cargarCarritoLocal() {
  try {
    const data = localStorage.getItem('carrito')
    carrito.value = data ? JSON.parse(data) : []
  } catch {
    carrito.value = []
    localStorage.removeItem('carrito')

    $q.notify({
      type: 'warning',
      message: 'El carrito se reinició por datos inválidos',
      caption: 'Se detectó información corrupta',
      icon: 'warning',
    })
  }
}

function guardarCarrito() {
  localStorage.setItem('carrito', JSON.stringify(carrito.value))
}

function agregarCarrito(servicio) {
  const existe = carrito.value.some((s) => s.id === servicio.id)

  if (existe) {
    $q.notify({
      type: 'warning',
      message: 'Este servicio ya está en tu carrito',
      icon: 'info',
    })
    return
  }

  carrito.value.push({ ...servicio, cantidad: 1 })
  guardarCarrito()

  $q.notify({
    type: 'positive',
    message: 'Servicio agregado correctamente',
    caption: servicio.nombre,
    icon: 'check_circle',
  })
}

function confirmarIrCarrito() {
  if (totalItems.value === 0) {
    $q.notify({
      type: 'info',
      message: 'Tu carrito está vacío',
      icon: 'shopping_cart',
    })
    return
  }

  $q.dialog({
    title: 'Ir al carrito',
    message: '¿Deseas revisar tu carrito ahora?',
    ok: 'Sí, ir',
    cancel: 'Seguir viendo',
    persistent: true,
  }).onOk(() => {
    router.push('/carrito')
  })
}

onMounted(() => {
  cargarServicios()
  cargarCarritoLocal()
})
</script>

<style scoped>
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
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.my-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.12);
}

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

.mapa-container {
  max-width: 1100px;
  margin: auto;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.footer {
  background: #2f2f2f;
  color: white;
  text-align: center;
  padding: 20px;
}
@media (max-width: 600px) {
  .footer {
    padding: 8px 12px;
    font-size: 12px;
    line-height: 1.2;
  }

  .footer p {
    margin: 4px 0;
  }
}

.carrito-flotante {
  z-index: 9999;
}

.hero-content {
  animation: heroFade 1.4s ease-out forwards;
}

@keyframes heroFade {
  from {
    opacity: 0;
    transform: translateY(-50%) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) scale(1);
  }
}

.my-card {
  animation: cardFadeUp 0.8s ease both;
}

@keyframes cardFadeUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.my-card:hover {
  animation: spaFloat 0.6s ease-in-out forwards;
}

@keyframes spaFloat {
  from {
    transform: translateY(-6px);
  }
  to {
    transform: translateY(-10px);
  }
}

.galeria-grid img {
  animation: galleryFade 1s ease both;
}

@keyframes galleryFade {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.galeria-grid img:hover {
  transform: scale(1.08);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.25);
}

.horarios-card {
  animation: horariosFade 1s ease-out both;
}

@keyframes horariosFade {
  from {
    opacity: 0;
    transform: translateY(25px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.carrito-flotante .q-btn {
  animation: carritoPulse 2.5s infinite ease-in-out;
}

@keyframes carritoPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.06);
  }
  100% {
    transform: scale(1);
  }
}

.q-badge {
  animation: badgeBounce 1.4s ease-in-out infinite;
}

@keyframes badgeBounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}
</style>
