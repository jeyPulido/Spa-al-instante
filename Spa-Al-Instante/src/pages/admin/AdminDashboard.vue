<template>
  <q-page class="q-pa-lg bg-grey-2">
    <!-- HEADER -->
    <div class="row items-center q-mb-lg">
      <q-icon name="dashboard" size="32px" color="primary" />
      <p class="text-h5 text-weight-bold q-ml-sm">Dashboard Administrativo</p>
    </div>

    <!-- FILTROS -->
    <q-card class="q-mb-lg">
      <q-card-section class="row q-col-gutter-md items-end">
        <div class="col-12 col-md-3">
          <q-input filled v-model="filtroInicio" label="Fecha inicio" type="date" />
        </div>
        <div class="col-12 col-md-3">
          <q-input filled v-model="filtroFin" label="Fecha fin" type="date" />
        </div>

        <div class="col-12 col-md-3 row q-gutter-sm">
          <q-btn color="primary" label="Aplicar" icon="filter_alt" @click="aplicarFiltros" />
          <q-btn flat color="grey-7" label="Limpiar" icon="clear" @click="limpiarFiltros" />
        </div>
      </q-card-section>
    </q-card>

    <!-- KPIs -->
    <div class="row q-col-gutter-lg q-mb-lg">
      <MetricCard title="💰 Ingresos" :value="`$ ${totalIngresos}`" />
      <MetricCard title="📊 Citas atendidas" :value="totalCitas" />
      <MetricCard title="🔁 Clientes recurrentes" :value="clientesRecurrentes" />
      <MetricCard title="❌ % Cancelaciones" :value="`${porcentajeCanceladas}%`" />
      <MetricCard title="📈 Variación mensual" :value="variacionMes" />
    </div>

    <!-- GRÁFICAS -->
    <div class="row q-col-gutter-lg q-mb-lg">
      <div class="col-12 col-md-6">
        <q-card>
          <q-card-section>
            <p class="text-weight-bold">Ventas por día</p>
            <canvas ref="chartPorDia"></canvas>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-12 col-md-6">
        <q-card>
          <q-card-section>
            <p class="text-weight-bold">Ingresos por servicio</p>
            <canvas ref="chartPorServicio"></canvas>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <!-- RANKING -->
    <q-card class="q-mb-lg">
      <q-card-section>
        <p class="text-h6">🏆 Ranking de clientes</p>
        <q-table :rows="rankingClientes" :columns="columnasRanking" row-key="cliente" dense />
      </q-card-section>
    </q-card>

    <!-- EXPORTAR -->
    <q-card>
      <q-card-section class="row items-center justify-between">
        <p class="text-h6">📄 Exportar citas</p>
        <q-btn color="primary" icon="picture_as_pdf" label="Exportar PDF" @click="exportarPDF" />
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { Chart } from 'chart.js/auto'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'

const citas = ref([])
const citasFiltradas = ref([])

const filtroInicio = ref('')
const filtroFin = ref('')
const mesRanking = ref('')

const chartPorDia = ref(null)
const chartPorServicio = ref(null)
let chartDia, chartServicio

/* ================== CARGA ================== */
async function cargarCitas() {
  const res = await axios.get('http://localhost:8082/api/citas')
  citas.value = res.data
  citasFiltradas.value = [...citas.value]
  recalcular()
}

/* ================== FILTROS ================== */
function aplicarFiltros() {
  citasFiltradas.value = citas.value.filter((c) => {
    const f = c.fechaHora.split('T')[0]
    return (
      (!filtroInicio.value || f >= filtroInicio.value) && (!filtroFin.value || f <= filtroFin.value)
    )
  })
  recalcular()
}

function limpiarFiltros() {
  filtroInicio.value = ''
  filtroFin.value = ''
  mesRanking.value = ''
  citasFiltradas.value = [...citas.value]
  recalcular()
}

/* ================== COMPUTEDS ================== */
const atendidas = computed(() => citasFiltradas.value.filter((c) => c.estado === 'ATENDIDA'))
const canceladas = computed(() => citasFiltradas.value.filter((c) => c.estado === 'CANCELADA'))

const totalCitas = computed(() => atendidas.value.length)

const totalIngresos = computed(() =>
  atendidas.value.reduce((s, c) => s + c.servicios.reduce((a, b) => a + b.precio, 0), 0),
)

const clientesRecurrentes = computed(() => {
  const map = {}
  atendidas.value.forEach((c) => {
    const n = c.nombreCliente
    map[n] = (map[n] || 0) + 1
  })
  return Object.values(map).filter((v) => v >= 2).length
})

const porcentajeCanceladas = computed(() => {
  if (!citasFiltradas.value.length) return 0
  return ((canceladas.value.length / citasFiltradas.value.length) * 100).toFixed(1)
})

const variacionMes = computed(() => {
  const mesActual = new Date().toISOString().slice(0, 7)
  const mesAnterior = new Date(new Date().setMonth(new Date().getMonth() - 1))
    .toISOString()
    .slice(0, 7)

  const totalMes = (m) =>
    atendidas.value
      .filter((c) => c.fechaHora.startsWith(m))
      .reduce((s, c) => s + c.servicios.reduce((a, b) => a + b.precio, 0), 0)

  const actual = totalMes(mesActual)
  const anterior = totalMes(mesAnterior)

  if (!anterior) return '—'
  const pct = (((actual - anterior) / anterior) * 100).toFixed(1)
  return `${pct}%`
})

/* ================== RANKING ================== */
const rankingClientes = computed(() => {
  const map = {}
  atendidas.value.forEach((c) => {
    if (mesRanking.value && !c.fechaHora.startsWith(mesRanking.value)) return
    const n = `${c.nombreCliente} ${c.apellidosCliente}`
    const t = c.servicios.reduce((a, b) => a + b.precio, 0)
    if (!map[n]) map[n] = { cliente: n, citas: 0, total: 0 }
    map[n].citas++
    map[n].total += t
  })
  return Object.values(map).sort((a, b) => b.total - a.total)
})

const columnasRanking = [
  { name: 'cliente', label: 'Cliente', field: 'cliente' },
  { name: 'citas', label: 'Citas', field: 'citas', align: 'center' },
  { name: 'total', label: 'Total', field: 'total', format: (v) => `$ ${v}` },
]

/* ================== GRÁFICAS ================== */
function generarPorDia() {
  const map = {}
  atendidas.value.forEach((c) => {
    const f = c.fechaHora.split('T')[0]
    map[f] = (map[f] || 0) + c.servicios.reduce((a, b) => a + b.precio, 0)
  })
  if (chartDia) chartDia.destroy()
  chartDia = new Chart(chartPorDia.value, {
    type: 'bar',
    data: { labels: Object.keys(map), datasets: [{ data: Object.values(map) }] },
  })
}

function generarPorServicio() {
  const map = {}
  atendidas.value.forEach((c) =>
    c.servicios.forEach((s) => (map[s.nombre] = (map[s.nombre] || 0) + s.precio)),
  )
  if (chartServicio) chartServicio.destroy()
  chartServicio = new Chart(chartPorServicio.value, {
    type: 'pie',
    data: { labels: Object.keys(map), datasets: [{ data: Object.values(map) }] },
  })
}

function recalcular() {
  generarPorDia()
  generarPorServicio()
}

/* ================== PDF ================== */
function exportarPDF() {
  const doc = new jsPDF()
  doc.text('Reporte de Citas', 14, 15)

  autoTable(doc, {
    startY: 20,
    head: [['Cliente', 'Fecha', 'Estado', 'Total']],
    body: citasFiltradas.value.map((c) => [
      `${c.nombreCliente} ${c.apellidosCliente}`,
      c.fechaHora,
      c.estado,
      `$ ${c.servicios.reduce((a, b) => a + b.precio, 0)}`,
    ]),
  })

  doc.text(`Total: $ ${totalIngresos.value}`, 14, doc.lastAutoTable.finalY + 10)
  doc.save('citas.pdf')
}

onMounted(cargarCitas)
</script>
<style scoped>
.q-page {
  max-width: 1400px;
  margin: auto;
}
</style>
