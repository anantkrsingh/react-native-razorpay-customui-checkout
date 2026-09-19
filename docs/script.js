(function () {
  'use strict';

  var root = document.documentElement;
  var themeToggle = document.getElementById('themeToggle');
  var THEME_KEY = 'rzp-docs-theme';

  function applyTheme(theme) {
    if (theme === 'light' || theme === 'dark') {
      root.setAttribute('data-theme', theme);
    } else {
      root.removeAttribute('data-theme');
    }
  }

  function getStoredTheme() {
    try {
      return localStorage.getItem(THEME_KEY);
    } catch (e) {
      return null;
    }
  }

  function storeTheme(theme) {
    try {
      localStorage.setItem(THEME_KEY, theme);
    } catch (e) {
      /* ignore */
    }
  }

  applyTheme(getStoredTheme());

  if (themeToggle) {
    themeToggle.addEventListener('click', function () {
      var prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      var current = root.getAttribute('data-theme') || (prefersDark ? 'dark' : 'light');
      var next = current === 'dark' ? 'light' : 'dark';
      applyTheme(next);
      storeTheme(next);
    });
  }

  // Mobile nav toggle
  var navToggle = document.getElementById('navToggle');
  var sidebar = document.getElementById('sidebar');
  var backdrop = document.getElementById('sidebarBackdrop');

  function closeNav() {
    sidebar.classList.remove('is-open');
    backdrop.classList.remove('is-open');
    navToggle.setAttribute('aria-expanded', 'false');
  }

  function openNav() {
    sidebar.classList.add('is-open');
    backdrop.classList.add('is-open');
    navToggle.setAttribute('aria-expanded', 'true');
  }

  if (navToggle) {
    navToggle.addEventListener('click', function () {
      var isOpen = sidebar.classList.contains('is-open');
      if (isOpen) closeNav(); else openNav();
    });
  }
  if (backdrop) backdrop.addEventListener('click', closeNav);

  var navLinks = Array.prototype.slice.call(document.querySelectorAll('.nav-link'));
  navLinks.forEach(function (link) {
    link.addEventListener('click', function () {
      if (window.innerWidth <= 960) closeNav();
    });
  });

  // Scroll-spy: highlight the active section link
  var targets = navLinks
    .map(function (link) {
      var id = link.getAttribute('href').slice(1);
      var el = document.getElementById(id);
      return el ? { link: link, el: el } : null;
    })
    .filter(Boolean);

  function setActive(id) {
    navLinks.forEach(function (link) {
      link.classList.toggle('active', link.getAttribute('href') === '#' + id);
    });
  }

  if ('IntersectionObserver' in window && targets.length) {
    var observer = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            var id = entry.target.id;
            setActive(id);
          }
        });
      },
      { rootMargin: '-40% 0px -55% 0px', threshold: 0 }
    );
    targets.forEach(function (t) {
      observer.observe(t.el);
    });
  }

  // Sidebar search/filter across API + card-utility nav items
  var searchInput = document.getElementById('searchInput');
  var filterableLists = [
    document.getElementById('apiNavList'),
    document.getElementById('cardNavList'),
  ].filter(Boolean);

  if (searchInput) {
    searchInput.addEventListener('input', function () {
      var query = searchInput.value.trim().toLowerCase();
      filterableLists.forEach(function (list) {
        Array.prototype.slice.call(list.children).forEach(function (li) {
          var link = li.querySelector('a');
          var haystack = (
            (link.textContent || '') + ' ' + (link.getAttribute('data-search') || '')
          ).toLowerCase();
          var matches = query === '' || haystack.indexOf(query) !== -1;
          li.style.display = matches ? '' : 'none';
        });
      });
    });
  }

  // Copy-to-clipboard for code blocks
  var copyButtons = Array.prototype.slice.call(document.querySelectorAll('.copy-btn'));
  copyButtons.forEach(function (btn) {
    btn.addEventListener('click', function () {
      var block = btn.closest('.code-block');
      var codeEl = block ? block.querySelector('code') : null;
      if (!codeEl) return;
      var text = codeEl.textContent;

      function markCopied() {
        var original = btn.textContent;
        btn.textContent = 'Copied!';
        btn.classList.add('copied');
        setTimeout(function () {
          btn.textContent = original;
          btn.classList.remove('copied');
        }, 1600);
      }

      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(markCopied, function () {
          fallbackCopy(text, markCopied);
        });
      } else {
        fallbackCopy(text, markCopied);
      }
    });
  });

  function fallbackCopy(text, done) {
    var textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    try {
      document.execCommand('copy');
      done();
    } catch (e) {
      /* ignore */
    } finally {
      document.body.removeChild(textarea);
    }
  }
})();
