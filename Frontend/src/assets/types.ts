export interface License {
    id: number;
    name: string;
}

export interface Zone {
    id: number;
    name: string;
    capacity: number;
    isPickerZone: boolean;
    pickerTasks: PickerTask[];
    tasks: Task[];
    workers: Worker[];
}

export interface Task {
    id: number;
    name: string;
    description: string;
    minWorkers: number;
    maxWorkers: number;
    requiredLicense: License[];
    zoneId: number;
    maxTime: number;
    minTime: number;
}

export interface ActiveTask {
    id: number;
    dueDate: string;
    startTime: string;
    endTime: string;
    mcStartTime: string
    mcEndTime: string;
    eta: string;
    date: string;
    task: Task;
    workers: Worker[];
    strictStart: boolean;
}

export interface Worker {
    id: number;
    name: string;
    zone: number;
    efficiency: number;
    availability: boolean;
    dead: boolean;
    licenses: License[];
    currentTaskId: number;
}

export interface TimeTable {
    id: number;
    startTime: string;
    endTime: string;
    realStartTime: string;
    realEndTime: string;
    workerId: number;
}

export interface PickerTask {
    id: number;
    distance: number;
    packAmount: number;
    linesAmount: number;
    weight: number;
    volume: number;
    avgHeight: number;
    time: number;
    startTime: string;
    endTime: string;
    mcStartTime: string
    mcEndTime: string;
    date: string;
    zoneId: number;
    worker: Worker;
}

export interface WorldSimObject {
    id: number;
    time: string;
    completedTasks: number;
    itemsPicked: number;
    realData: boolean;
    zoneId: number;
}

export interface Notification {
    id: number;
    message: string;
    zoneId: number ;
    timestamp: string | null;
}

export const ErrorCodes = new Map<number, string>([
    [101, "No Tasks"],
    [102, "No Workers in Zone"],
    [103, "No Workers in Zone coming to work today"],
    [104, "Task could not complete due to workers going home"],
    [105, "No qualified workers for activetask in zone"],
]);
