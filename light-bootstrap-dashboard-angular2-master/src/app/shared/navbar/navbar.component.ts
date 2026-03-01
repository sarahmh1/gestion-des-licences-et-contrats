import { Component, OnInit, OnDestroy, ElementRef } from '@angular/core';
import { ROUTES } from '../../sidebar/sidebar.component';
import { Location } from '@angular/common';
import { AuthService } from '../../auth/AuthService';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MessageService, ConversationDTO, MessageDTO } from '../../Services/message.service';
import { UserService } from '../../Services/user.service';
import { WebsocketService } from '../../Services/websocket.service';
import { NotificationAppService } from '../../Services/notification.service';
import { AppNotification } from '../../Model/Notification';
import { Subscription } from 'rxjs';

interface UserDTO {
    id: number;
    firstname: string;
    lastname: string;
    email: string;
    profilePicture?: string;
}

@Component({
    selector: 'navbar-cmp',
    templateUrl: 'navbar.component.html',
    styleUrls: ['navbar.component.scss']
})
export class NavbarComponent implements OnInit, OnDestroy {
    private listTitles: any[];
    location: Location;
    private toggleButton: any;
    private sidebarVisible: boolean;
    userName: string = '';
    userProfilePic: string = '';
    showDropdown: boolean = false;

    // Messaging
    showMessagingPanel: boolean = false;
    messagingView: 'conversations' | 'users' | 'chat' = 'conversations';
    allUsers: UserDTO[] = [];
    filteredUsers: UserDTO[] = [];
    userSearchQuery: string = '';
    conversations: ConversationDTO[] = [];
    selectedChatUser: UserDTO | null = null;
    selectedConversation: ConversationDTO | null = null;
    messages: MessageDTO[] = [];
    newMessage: string = '';
    currentUserId: number = 0;
    unreadTotal: number = 0;
    isTyping: boolean = false;
    typingTimeout: any;
    selectedFile: File | null = null;

    // Notifications
    notifications: AppNotification[] = [];
    unreadNotifCount: number = 0;
    showNotifPanel: boolean = false;
    private notifInterval: any;

    // Calendar
    showCalendarModal: boolean = false;
    calendarEvents: any[] = [];
    calMonth: number = new Date().getMonth();
    calYear: number = new Date().getFullYear();
    calMonthNames: string[] = [
        'Janvier', 'F\u00e9vrier', 'Mars', 'Avril', 'Mai', 'Juin',
        'Juillet', 'Ao\u00fbt', 'Septembre', 'Octobre', 'Novembre', 'D\u00e9cembre'
    ];
    calWeekDays: string[] = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
    calWeeks: any[][] = [];
    selectedCalDay: any = null;
    selectedDayEvents: any[] = [];

    private messageSubscription?: Subscription;
    private typingSubscription?: Subscription;

    constructor(
        location: Location,
        private element: ElementRef,
        private authService: AuthService,
        private router: Router,
        private http: HttpClient,
        private messageService: MessageService,
        private userService: UserService,
        private websocketService: WebsocketService,
        private notificationAppService: NotificationAppService
    ) {
        this.location = location;
        this.sidebarVisible = false;
    }

    ngOnInit() {
        this.listTitles = ROUTES.filter(listTitle => listTitle);
        const navbar: HTMLElement = this.element.nativeElement;
        this.toggleButton = navbar.getElementsByClassName('navbar-toggle')[0];

        this.loadUserProfile();
        this.initMessaging();
        this.loadNotifUnreadCount();
        this.notifInterval = setInterval(() => this.loadNotifUnreadCount(), 30000);
    }

    ngOnDestroy(): void {
        this.messageSubscription?.unsubscribe();
        this.typingSubscription?.unsubscribe();
        if (this.notifInterval) clearInterval(this.notifInterval);
    }

    loadUserProfile() {
        const token = this.authService.getToken();
        if (!token) return;

        const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
        this.http.get<any>('http://localhost:8089/Users/me', { headers }).subscribe(
            (user) => {
                const first = user.firstname || '';
                const last = user.lastname || '';
                this.userName = (first + ' ' + last).trim() || user.email || 'Utilisateur';
                this.currentUserId = user.id;
                this.loadNotifUnreadCount();

                if (user.profilePicture) {
                    this.userProfilePic = 'http://localhost:8089' + user.profilePicture;
                }
            },
            (err) => {
                const user = this.authService.getUser();
                if (user) {
                    this.userName = user.email || 'Utilisateur';
                    this.currentUserId = user.userId || user.id || 0;
                }
            }
        );
    }

    // ========== Messaging ==========

    initMessaging() {
        const currentUser = this.authService.getUser();
        if (currentUser) {
            this.currentUserId = currentUser.id || currentUser.userId || 0;
        }

        if (this.currentUserId > 0) {
            this.websocketService.connect(this.currentUserId.toString());
            this.loadConversations();
            this.loadUnreadCount();

            this.messageSubscription = this.websocketService.getMessages().subscribe(message => {
                if (message) {
                    this.handleNewMessage(message);
                }
            });

            this.typingSubscription = this.websocketService.getTypingIndicators().subscribe(userId => {
                if (userId && this.selectedChatUser && userId === this.selectedChatUser.id) {
                    this.isTyping = true;
                    clearTimeout(this.typingTimeout);
                    this.typingTimeout = setTimeout(() => {
                        this.isTyping = false;
                    }, 3000);
                }
            });
        }
    }

    loadConversations() {
        if (this.currentUserId <= 0) return;
        this.messageService.getUserConversations(this.currentUserId).subscribe(
            conversations => {
                this.conversations = conversations.sort((a, b) => {
                    const tA = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
                    const tB = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
                    return tB - tA;
                });
            },
            error => { console.error('Error loading conversations:', error); }
        );
    }

    loadUnreadCount() {
        if (this.currentUserId <= 0) return;
        this.messageService.getUnreadCount(this.currentUserId).subscribe(
            (res: any) => { this.unreadTotal = (typeof res === 'object' && res.count !== undefined) ? res.count : res; },
            error => { console.error('Error loading unread count:', error); }
        );
    }

    toggleMessagingPanel() {
        this.showMessagingPanel = !this.showMessagingPanel;
        this.showDropdown = false;
        if (this.showMessagingPanel) {
            this.messagingView = 'conversations';
            this.loadConversations();
            this.unreadTotal = 0;
        }
    }

    closeMessagingPanel() {
        this.showMessagingPanel = false;
        this.messagingView = 'conversations';
        this.selectedChatUser = null;
        this.messages = [];
    }

    showNewMessage() {
        this.messagingView = 'users';
        this.loadAllUsers();
    }

    selectConversation(conv: any) {
        conv.unreadCount = 0;
        const user: any = {
            id: conv.otherUserId,
            firstname: conv.otherUserName?.split(' ')[0] || '',
            lastname: conv.otherUserName?.split(' ').slice(1).join(' ') || '',
            email: conv.otherUserEmail,
            profilePicture: conv.otherUserProfilePicture
        };
        this.selectUser(user);
    }

    loadAllUsers() {
        this.userService.getAllUsers().subscribe(
            (users: any[]) => {
                this.allUsers = users
                    .filter(u => u.id !== this.currentUserId)
                    .map(u => ({
                        id: u.id,
                        firstname: u.firstname || u.firstName || '',
                        lastname: u.lastname || u.lastName || '',
                        email: u.email || '',
                        profilePicture: u.profilePicture || u.ProfilePicture
                    }));
                this.filteredUsers = this.allUsers;
            },
            error => { console.error('Error loading users:', error); }
        );
    }

    searchUsers() {
        if (!this.userSearchQuery.trim()) {
            this.filteredUsers = this.allUsers;
            return;
        }
        const query = this.userSearchQuery.toLowerCase();
        this.filteredUsers = this.allUsers.filter(u =>
            u.firstname.toLowerCase().includes(query) ||
            u.lastname.toLowerCase().includes(query) ||
            u.email.toLowerCase().includes(query)
        );
    }

    getUserFullName(user: UserDTO): string {
        return `${user.firstname} ${user.lastname}`.trim() || user.email;
    }

    getProfilePictureUrl(profilePicture: string | undefined | null, name: string): string {
        if (!profilePicture) {
            return 'https://ui-avatars.com/api/?name=' + encodeURIComponent(name || 'U') + '&background=667eea&color=fff&size=128';
        }
        if (profilePicture.startsWith('http://') || profilePicture.startsWith('https://')) {
            return profilePicture;
        }
        return 'http://localhost:8089' + (profilePicture.startsWith('/') ? '' : '/') + profilePicture;
    }

    selectUser(user: UserDTO) {
        this.selectedChatUser = user;
        this.messagingView = 'chat';
        this.messages = [];
        this.newMessage = '';

        // Load messages
        this.messageService.getConversationMessages(this.currentUserId, user.id).subscribe(
            messages => {
                this.messages = messages;
                setTimeout(() => this.scrollToBottom(), 100);
            },
            error => { console.error('Error loading messages:', error); }
        );

        // Mark as read
        this.messageService.markMessagesAsRead(user.id, this.currentUserId).subscribe(() => {
            this.loadUnreadCount();
        });
    }

    backToConversations() {
        this.messagingView = 'conversations';
        this.selectedChatUser = null;
        this.messages = [];
    }

    deleteConversation(conv: any) {
        if (!confirm('Supprimer cette conversation ?')) return;
        this.messageService.deleteConversation(conv.id, this.currentUserId).subscribe(
            () => {
                this.conversations = this.conversations.filter(c => c.id !== conv.id);
            },
            error => { console.error('Error deleting conversation:', error); }
        );
    }

    deleteMessage(msg: any) {
        if (!msg.id) return;
        this.messageService.deleteMessage(msg.id).subscribe(
            () => {
                this.messages = this.messages.filter(m => m.id !== msg.id);
            },
            error => { console.error('Error deleting message:', error); }
        );
    }

    sendMessage() {
        if (!this.newMessage.trim() || !this.selectedChatUser) return;

        const message: MessageDTO = {
            content: this.newMessage,
            senderId: this.currentUserId,
            receiverId: this.selectedChatUser.id
        };

        const tempMessage: MessageDTO = {
            ...message,
            timestamp: new Date()
        };
        this.messages.push(tempMessage);
        this.newMessage = '';
        setTimeout(() => this.scrollToBottom(), 100);

        this.messageService.sendMessage(message).subscribe(
            savedMessage => {
                const index = this.messages.indexOf(tempMessage);
                if (index > -1) {
                    this.messages[index] = savedMessage;
                }
                this.loadConversations();
            },
            error => {
                const index = this.messages.indexOf(tempMessage);
                if (index > -1) {
                    this.messages.splice(index, 1);
                }
                console.error('Error sending message:', error);
            }
        );
    }

    onTyping() {
        if (this.selectedChatUser) {
            this.websocketService.sendTypingIndicator(this.currentUserId, this.selectedChatUser.id);
        }
    }

    handleNewMessage(message: MessageDTO) {
        const isCurrentChat = this.selectedChatUser &&
            (message.senderId === this.selectedChatUser.id || message.receiverId === this.selectedChatUser.id);

        if (isCurrentChat) {
            // We're in the chat — add message and mark as read immediately
            if (!this.messages.find(m => m.id === message.id)) {
                this.messages.push(message);
                setTimeout(() => this.scrollToBottom(), 100);
            }
            // Mark as read on server so unread count stays correct
            if (message.senderId !== this.currentUserId) {
                this.messageService.markMessagesAsRead(message.senderId, this.currentUserId).subscribe();
            }
        } else {
            // Message from someone else — update the unread badge
            if (message.senderId !== this.currentUserId && !this.showMessagingPanel) {
                this.loadUnreadCount();
            }
        }
    }

    scrollToBottom() {
        const chatContainer = document.querySelector('.msg-chat-messages');
        if (chatContainer) {
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }
    }

    formatTime(timestamp: Date): string {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }

    isImage(fileType: string | undefined): boolean {
        return fileType?.startsWith('image/') || false;
    }

    getFileDownloadUrl(filePath: string | undefined): string {
        if (!filePath) return '';
        return this.messageService.getFileDownloadUrl(filePath);
    }

    isAudio(fileType: string | undefined): boolean {
        return fileType?.startsWith('audio/') || false;
    }

    triggerFileInput() {
        const fileInput = document.getElementById('msgFileInput') as HTMLInputElement;
        if (fileInput) {
            fileInput.click();
        }
    }

    onFileSelected(event: any) {
        const file = event.target.files[0];
        if (file) {
            this.selectedFile = file;
        }
    }

    removeSelectedFile() {
        this.selectedFile = null;
        const fileInput = document.getElementById('msgFileInput') as HTMLInputElement;
        if (fileInput) {
            fileInput.value = '';
        }
    }

    sendFile() {
        if (!this.selectedFile || !this.selectedChatUser) return;

        this.messageService.sendMessageWithFile(
            this.currentUserId,
            this.selectedChatUser.id,
            this.newMessage,
            this.selectedFile
        ).subscribe(
            message => {
                this.messages.push(message);
                this.newMessage = '';
                this.selectedFile = null;
                const fileInput = document.getElementById('msgFileInput') as HTMLInputElement;
                if (fileInput) { fileInput.value = ''; }
                setTimeout(() => this.scrollToBottom(), 100);
                this.loadConversations();
            },
            error => {
                console.error('Error sending file:', error);
            }
        );
    }

    // ========== User Dropdown ==========

    toggleDropdown() {
        this.showDropdown = !this.showDropdown;
        this.showMessagingPanel = false;
    }

    closeDropdown() {
        this.showDropdown = false;
    }

    goToProfile() {
        this.showDropdown = false;
        this.router.navigate(['/profile']);
    }

    logout() {
        this.showDropdown = false;
        this.authService.logout();
    }

    // ========== Sidebar ==========

    sidebarOpen() {
        const toggleButton = this.toggleButton;
        const body = document.getElementsByTagName('body')[0];
        setTimeout(function () {
            toggleButton.classList.add('toggled');
        }, 500);
        body.classList.add('nav-open');
        this.sidebarVisible = true;
    }

    sidebarClose() {
        const body = document.getElementsByTagName('body')[0];
        this.toggleButton.classList.remove('toggled');
        this.sidebarVisible = false;
        body.classList.remove('nav-open');
    }

    sidebarToggle() {
        if (this.sidebarVisible === false) {
            this.sidebarOpen();
        } else {
            this.sidebarClose();
        }
    }

    getTitle() {
        var titlee = this.location.prepareExternalUrl(this.location.path());
        if (titlee.charAt(0) === '#') {
            titlee = titlee.slice(1);
        }
        for (var item = 0; item < this.listTitles.length; item++) {
            if (this.listTitles[item].path === titlee) {
                return this.listTitles[item].title;
            }
        }
        return 'Dashboard';
    }

    // ========== Notifications ==========

    loadNotifUnreadCount(): void {
        if (!this.currentUserId) return;
        this.notificationAppService.getUnreadCount(this.currentUserId).subscribe(
            (res) => { this.unreadNotifCount = res.count; },
            (err) => { console.error('Error loading unread count:', err); }
        );
    }

    loadNotifications(): void {
        if (!this.currentUserId) return;
        this.notificationAppService.getNotificationsForUser(this.currentUserId).subscribe(
            (notifs) => { this.notifications = notifs; },
            (err) => { console.error('Error loading notifications:', err); }
        );
    }

    toggleNotifPanel(): void {
        this.showNotifPanel = !this.showNotifPanel;
        if (this.showNotifPanel) {
            this.loadNotifications();
        }
    }

    markNotifRead(notif: AppNotification): void {
        if (!notif.read) {
            this.notificationAppService.markAsRead(notif.id).subscribe(() => {
                notif.read = true;
                this.unreadNotifCount = Math.max(0, this.unreadNotifCount - 1);
            });
        }
    }

    markAllNotifsRead(): void {
        if (!this.currentUserId) return;
        this.notificationAppService.markAllAsRead(this.currentUserId).subscribe(() => {
            this.notifications.forEach(n => n.read = true);
            this.unreadNotifCount = 0;
        });
    }

    getTimeAgo(dateStr: string): string {
        const date = new Date(dateStr);
        const now = new Date();
        const diffMs = now.getTime() - date.getTime();
        const diffMins = Math.floor(diffMs / 60000);
        if (diffMins < 1) return "À l'instant";
        if (diffMins < 60) return diffMins + ' min';
        const diffHours = Math.floor(diffMins / 60);
        if (diffHours < 24) return diffHours + 'h';
        const diffDays = Math.floor(diffHours / 24);
        if (diffDays < 7) return diffDays + 'j';
        return date.toLocaleDateString('fr-FR');
    }

    // ========== Calendar ==========

    toggleCalendarModal(): void {
        this.showCalendarModal = !this.showCalendarModal;
        if (this.showCalendarModal) {
            this.loadCalendarEvents();
        }
    }

    loadCalendarEvents(): void {
        if (!this.currentUserId) return;
        const token = this.authService.getToken();
        if (!token) return;
        const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
        this.http.get<any[]>(
            `http://localhost:8089/InterventionPreventive/calendar/${this.currentUserId}`,
            { headers }
        ).subscribe(
            (events) => { this.calendarEvents = events; this.buildCalGrid(); },
            () => { this.buildCalGrid(); }
        );
    }

    buildCalGrid(): void {
        this.calWeeks = [];
        this.selectedCalDay = null;
        this.selectedDayEvents = [];
        const totalDays = new Date(this.calYear, this.calMonth + 1, 0).getDate();
        const firstDow = new Date(this.calYear, this.calMonth, 1).getDay();
        // Convert Sunday=0 to Monday-based: Mon=0..Sun=6
        const offset = firstDow === 0 ? 6 : firstDow - 1;
        const today = new Date(); today.setHours(0, 0, 0, 0);
        const cells: any[] = [];
        // Empty cells for offset
        for (let i = 0; i < offset; i++) { cells.push(null); }
        for (let d = 1; d <= totalDays; d++) {
            const date = new Date(this.calYear, this.calMonth, d);
            const dateStr = this.calToDateStr(date);
            cells.push({
                dayNum: d,
                dateStr: dateStr,
                isToday: date.getTime() === today.getTime(),
                dots: this.getDayDots(dateStr)
            });
        }
        // Fill remaining cells to complete the last week
        while (cells.length % 7 !== 0) { cells.push(null); }
        // Split into weeks
        for (let i = 0; i < cells.length; i += 7) {
            this.calWeeks.push(cells.slice(i, i + 7));
        }
    }

    calToDateStr(d: Date): string {
        const y = d.getFullYear();
        const m = (d.getMonth() + 1).toString().padStart(2, '0');
        const day = d.getDate().toString().padStart(2, '0');
        return `${y}-${m}-${day}`;
    }

    getDayDots(dateStr: string): string[] {
        const colors: string[] = [];
        for (const ev of this.calendarEvents) {
            const start = ev.start || ev.end;
            const end = ev.end || ev.start;
            if (!start) continue;
            if (dateStr >= start && dateStr <= end) {
                if (!colors.includes(ev.color)) {
                    colors.push(ev.color);
                }
            }
        }
        return colors;
    }

    selectCalDay(day: any): void {
        if (!day) return;
        if (this.selectedCalDay?.dateStr === day.dateStr) {
            this.selectedCalDay = null;
            this.selectedDayEvents = [];
            return;
        }
        this.selectedCalDay = day;
        this.selectedDayEvents = this.calendarEvents.filter((ev: any) => {
            const start = ev.start || ev.end;
            const end = ev.end || ev.start;
            if (!start) return false;
            return day.dateStr >= start && day.dateStr <= end;
        });
    }

    prevCalMonth(): void {
        this.calMonth--;
        if (this.calMonth < 0) { this.calMonth = 11; this.calYear--; }
        this.loadCalendarEvents();
    }

    nextCalMonth(): void {
        this.calMonth++;
        if (this.calMonth > 11) { this.calMonth = 0; this.calYear++; }
        this.loadCalendarEvents();
    }

    getCalMonthLabel(): string {
        return this.calMonthNames[this.calMonth] + ' ' + this.calYear;
    }

    getCalTypeLabel(type: string): string {
        return type === 'recommandee' ? 'P\u00e9riode Recommand\u00e9e' : "P\u00e9riode d'Intervention";
    }

    goToIntervention(ev: any): void {
        this.showCalendarModal = false;
        this.router.navigate(['/interventions-preventives']);
    }
}
