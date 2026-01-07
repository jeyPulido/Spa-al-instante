<template>
  <q-layout view="lHh Lpr lFf">
    <!-- HEADER -->
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <!-- ☰ MENU MOBILE -->
        <q-btn flat dense round icon="menu" class="lt-md" @click="drawer = true" />

        <q-toolbar-title class="text-weight-bold"> Spa al Instante </q-toolbar-title>

        <!-- 🖥️ MENÚ DESKTOP -->
        <div v-if="auth.ready" class="gt-sm row items-center q-gutter-x-md">
          <!-- ✅ INICIO (TODOS) -->
          <q-btn flat label="Inicio" to="/" />

          <!-- 🟢 INVITADO -->
          <template v-if="isGuest">
            <q-btn flat label="Carrito" to="/carrito" />
            <q-btn flat label="Iniciar sesión" to="/admin/login" />
          </template>

          <!-- 👤 USUARIO -->
          <template v-else-if="isUser">
            <q-btn flat label="Carrito" to="/carrito" />
            <q-btn flat label="Historial" to="/historial" />
            <q-btn flat label="Perfil" to="/perfil" />
            <q-btn flat label="Cerrar sesión" @click="logout" />
          </template>

          <!-- 🛠️ ADMIN -->
          <template v-else-if="isAdmin">
            <q-btn flat label="Perfil" to="/perfil" />
            <q-btn flat label="Dashboard" to="/admin/dashboard" />
            <q-btn flat label="Citas" to="/admin/citas" />
            <q-btn flat label="Servicios" to="/admin/servicios" />
            <q-btn flat label="Registrar usuario" to="/admin/register" />
            <q-btn flat label="Cerrar sesión" @click="logout" />
          </template>
        </div>
        <div v-else class="row items-center q-gutter-x-md">
          <q-spinner color="white" size="sm" />
        </div>
      </q-toolbar>
    </q-header>

    <!-- 📱 DRAWER MOBILE -->
    <q-drawer v-if="auth.ready" v-model="drawer" side="left" overlay bordered>
      <q-list padding>
        <!-- ✅ INICIO (TODOS) -->
        <q-item clickable to="/">
          <q-item-section>Inicio</q-item-section>
        </q-item>

        <!-- 🟢 INVITADO -->
        <template v-if="isGuest">
          <q-item clickable to="/carrito">
            <q-item-section>Carrito</q-item-section>
          </q-item>

          <q-item clickable to="/admin/login">
            <q-item-section>Iniciar sesión</q-item-section>
          </q-item>
        </template>

        <!-- 👤 USUARIO -->
        <template v-else-if="isUser">
          <q-item clickable to="/carrito">
            <q-item-section>Carrito</q-item-section>
          </q-item>

          <q-item clickable to="/historial">
            <q-item-section>Historial</q-item-section>
          </q-item>

          <q-item clickable to="/perfil">
            <q-item-section>Perfil</q-item-section>
          </q-item>

          <q-item clickable @click="logout">
            <q-item-section>Cerrar sesión</q-item-section>
          </q-item>
        </template>

        <!-- 🛠️ ADMIN -->
        <template v-else-if="isAdmin">
          <q-item clickable to="/perfil">
            <q-item-section>Perfil</q-item-section>
          </q-item>

          <q-item clickable to="/admin/dashboard">
            <q-item-section>Dashboard</q-item-section>
          </q-item>

          <q-item clickable to="/admin/citas">
            <q-item-section>Citas</q-item-section>
          </q-item>

          <q-item clickable to="/admin/servicios">
            <q-item-section>Servicios</q-item-section>
          </q-item>

          <q-item clickable to="/admin/register">
            <q-item-section>Registrar usuario</q-item-section>
          </q-item>

          <q-item clickable @click="logout">
            <q-item-section>Cerrar sesión</q-item-section>
          </q-item>
        </template>
      </q-list>
    </q-drawer>

    <!-- CONTENIDO -->
    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from 'stores/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const drawer = ref(false)
const auth = useAuthStore()

const isGuest = computed(() => auth.ready && !auth.token)
const isUser = computed(() => auth.ready && auth.user?.rol === 'CLIENTE')
const isAdmin = computed(() => auth.ready && auth.user?.rol === 'ADMIN')

function logout() {
  auth.logout()
  drawer.value = false
  router.push('/')
}
</script>
